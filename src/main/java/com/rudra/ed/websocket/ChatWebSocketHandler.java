package com.rudra.ed.websocket;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import com.rudra.ed.dto.request.SendMessageRequest;
import com.rudra.ed.dto.response.MessageResponse;
import com.rudra.ed.security.UserPrincipal;
import com.rudra.ed.service.ConversationService;
import com.rudra.ed.service.MessageService;
import com.rudra.ed.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

	private final JsonMapper objectMapper;
	private final WebSocketSessionRegistry sessionRegistry;
	private final PresenceService presenceService;
	private final MessageService messageService;
	private final ConversationService conversationService;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		UserPrincipal principal = getPrincipal(session);
		if (principal == null) {
			closeQuietly(session, CloseStatus.NOT_ACCEPTABLE);
			return;
		}
		sessionRegistry.register(principal.getId(), session);
		presenceService.userConnected(principal.getId());
		log.debug("WS connected userId={} sessionId={}", principal.getId(), session.getId());
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		UserPrincipal principal = getPrincipal(session);
		if (principal == null) {
			sendError(session, "Unauthenticated WebSocket session");
			return;
		}

		WsEnvelope envelope;
		try {
			envelope = objectMapper.readValue(message.getPayload(), WsEnvelope.class);
		} catch (Exception ex) {
			sendError(session, "Invalid JSON envelope");
			return;
		}

		if (envelope.getType() == null) {
			sendError(session, "Missing message type");
			return;
		}

		try {
			switch (envelope.getType()) {
				case CHAT_MESSAGE -> handleChatMessage(session, principal, envelope.getPayload());
				case DELIVERED -> handleReceipt(session, principal, envelope.getPayload(), true);
				case READ -> handleReceipt(session, principal, envelope.getPayload(), false);
				case TYPING -> handleTyping(principal, envelope.getPayload());
				default -> sendError(session, "Unsupported message type: " + envelope.getType());
			}
		} catch (Exception ex) {
			log.warn("WS handler error for user {}: {}", principal.getId(), ex.getMessage());
			sendError(session, ex.getMessage() == null ? "Processing failed" : ex.getMessage());
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		Long userId = sessionRegistry.getUserId(session);
		sessionRegistry.remove(session);
		if (userId != null) {
			presenceService.userDisconnected(userId);
			log.debug("WS disconnected userId={} status={}", userId, status);
		}
	}

	private void handleChatMessage(WebSocketSession session, UserPrincipal principal, JsonNode payload) throws IOException {
		if (payload == null || !payload.hasNonNull("conversationId") || !payload.hasNonNull("content")) {
			sendError(session, "CHAT_MESSAGE requires conversationId and content");
			return;
		}

		SendMessageRequest request = new SendMessageRequest();
		request.setContent(payload.get("content").asText());
		if (payload.hasNonNull("clientMessageId")) {
			request.setClientMessageId(payload.get("clientMessageId").asText());
		}

		Long conversationId = payload.get("conversationId").asLong();
		MessageResponse saved = messageService.sendMessage(conversationId, principal.getId(), request);

		ObjectNode ackPayload = objectMapper.createObjectNode();
		ackPayload.put("clientMessageId", saved.getClientMessageId());
		ackPayload.set("message", objectMapper.valueToTree(saved));
		send(session, WsEnvelope.builder().type(WsMessageType.MESSAGE_ACK).payload(ackPayload).build());

		List<Long> memberIds = conversationService.memberUserIds(conversationId);
		presenceService.pushMessageToConversationMembers(memberIds, saved);
	}

	private void handleReceipt(WebSocketSession session, UserPrincipal principal, JsonNode payload, boolean delivered)
			throws IOException {
		if (payload == null || !payload.hasNonNull("messageId")) {
			sendError(session, "Receipt event requires messageId");
			return;
		}
		Long messageId = payload.get("messageId").asLong();
		MessageResponse updated = delivered
				? messageService.markDelivered(messageId, principal.getId())
				: messageService.markRead(messageId, principal.getId());

		ObjectNode receiptPayload = objectMapper.createObjectNode();
		receiptPayload.put("messageId", messageId);
		receiptPayload.put("userId", principal.getId());
		receiptPayload.put("status", delivered ? "DELIVERED" : "READ");
		receiptPayload.set("receipts", objectMapper.valueToTree(updated.getReceipts()));

		Long senderId = updated.getSenderId();
		presenceService.pushToUsers(
				List.of(senderId),
				delivered ? WsMessageType.DELIVERED : WsMessageType.READ,
				receiptPayload
		);

		send(session, WsEnvelope.builder()
				.type(delivered ? WsMessageType.DELIVERED : WsMessageType.READ)
				.payload(receiptPayload)
				.build());
	}

	private void handleTyping(UserPrincipal principal, JsonNode payload) {
		if (payload == null || !payload.hasNonNull("conversationId")) {
			return;
		}
		Long conversationId = payload.get("conversationId").asLong();
		conversationService.requireMembership(conversationId, principal.getId());

		ObjectNode typingPayload = objectMapper.createObjectNode();
		typingPayload.put("conversationId", conversationId);
		typingPayload.put("userId", principal.getId());
		typingPayload.put("displayName", principal.getDisplayName());
		typingPayload.put("typing", payload.path("typing").asBoolean(true));

		List<Long> members = conversationService.memberUserIds(conversationId).stream()
				.filter(id -> !id.equals(principal.getId()))
				.toList();
		presenceService.pushToUsers(members, WsMessageType.TYPING, typingPayload);
	}

	private UserPrincipal getPrincipal(WebSocketSession session) {
		Map<String, Object> attrs = session.getAttributes();
		Object value = attrs.get(JwtHandshakeInterceptor.USER_PRINCIPAL_ATTR);
		return value instanceof UserPrincipal principal ? principal : null;
	}

	private void send(WebSocketSession session, WsEnvelope envelope) throws IOException {
		synchronized (session) {
			session.sendMessage(new TextMessage(objectMapper.writeValueAsString(envelope)));
		}
	}

	private void sendError(WebSocketSession session, String message) throws IOException {
		ObjectNode payload = objectMapper.createObjectNode();
		payload.put("message", message);
		send(session, WsEnvelope.builder().type(WsMessageType.ERROR).payload(payload).build());
	}

	private void closeQuietly(WebSocketSession session, CloseStatus status) {
		try {
			session.close(status);
		} catch (IOException ignored) {
			// ignore
		}
	}
}
