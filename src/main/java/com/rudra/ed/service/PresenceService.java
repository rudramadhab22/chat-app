package com.rudra.ed.service;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import com.rudra.ed.dto.response.MessageResponse;
import com.rudra.ed.repository.ConversationMemberRepository;
import com.rudra.ed.websocket.WebSocketSessionRegistry;
import com.rudra.ed.websocket.WsEnvelope;
import com.rudra.ed.websocket.WsMessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PresenceService {

	private final UserService userService;
	private final ConversationMemberRepository memberRepository;
	private final WebSocketSessionRegistry sessionRegistry;
	private final JsonMapper objectMapper;

	public void userConnected(Long userId) {
		userService.setOnline(userId, true);
		broadcastPresence(userId, true);
	}

	public void userDisconnected(Long userId) {
		if (sessionRegistry.hasActiveSessions(userId)) {
			return;
		}
		userService.setOnline(userId, false);
		broadcastPresence(userId, false);
	}

	private void broadcastPresence(Long userId, boolean online) {
		Set<Long> contacts = memberRepository.findContactUserIds(userId);
		ObjectNode payload = objectMapper.createObjectNode();
		payload.put("userId", userId);
		payload.put("online", online);
		payload.put("lastSeenAt", Instant.now().toString());

		WsEnvelope envelope = WsEnvelope.builder()
				.type(WsMessageType.PRESENCE)
				.payload(payload)
				.build();

		try {
			String json = objectMapper.writeValueAsString(envelope);
			for (Long contactId : contacts) {
				sessionRegistry.sendToUser(contactId, json);
			}
		} catch (Exception ex) {
			log.warn("Failed to broadcast presence for user {}: {}", userId, ex.getMessage());
		}
	}

	public void pushToUsers(Iterable<Long> userIds, WsMessageType type, Object payloadObject) {
		try {
			WsEnvelope envelope = WsEnvelope.builder()
					.type(type)
					.payload(objectMapper.valueToTree(payloadObject))
					.build();
			String json = objectMapper.writeValueAsString(envelope);
			for (Long userId : userIds) {
				sessionRegistry.sendToUser(userId, json);
			}
		} catch (Exception ex) {
			log.warn("Failed to push {} event: {}", type, ex.getMessage());
		}
	}

	public void pushMessageToConversationMembers(Iterable<Long> memberIds, MessageResponse message) {
		pushToUsers(memberIds, WsMessageType.CHAT_MESSAGE, message);
	}
}
