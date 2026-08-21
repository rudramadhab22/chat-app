package com.rudra.ed.service;

import com.rudra.ed.domain.entity.Conversation;
import com.rudra.ed.domain.entity.ConversationMember;
import com.rudra.ed.domain.entity.Message;
import com.rudra.ed.domain.entity.MessageReceipt;
import com.rudra.ed.domain.entity.User;
import com.rudra.ed.domain.enums.MessageType;
import com.rudra.ed.domain.enums.ReceiptStatus;
import com.rudra.ed.dto.request.SendMessageRequest;
import com.rudra.ed.dto.response.MessageResponse;
import com.rudra.ed.exception.BadRequestException;
import com.rudra.ed.exception.ForbiddenException;
import com.rudra.ed.exception.ResourceNotFoundException;
import com.rudra.ed.repository.ConversationMemberRepository;
import com.rudra.ed.repository.ConversationRepository;
import com.rudra.ed.repository.MessageReceiptRepository;
import com.rudra.ed.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

	private final MessageRepository messageRepository;
	private final MessageReceiptRepository receiptRepository;
	private final ConversationRepository conversationRepository;
	private final ConversationMemberRepository memberRepository;
	private final ConversationService conversationService;
	private final UserService userService;

	@Transactional
	public MessageResponse sendMessage(Long conversationId, Long senderId, SendMessageRequest request) {
		conversationService.requireMembership(conversationId, senderId);
		Conversation conversation = conversationService.requireConversation(conversationId);
		User sender = userService.requireUser(senderId);

		Message message = Message.builder()
				.conversation(conversation)
				.sender(sender)
				.content(request.getContent().trim())
				.type(MessageType.TEXT)
				.clientMessageId(request.getClientMessageId())
				.build();
		Message saved = messageRepository.save(message);

		List<ConversationMember> members = memberRepository.findByConversationId(conversationId);
		List<MessageReceipt> receipts = new ArrayList<>();
		for (ConversationMember member : members) {
			if (member.getUser().getId().equals(senderId)) {
				continue;
			}
			receipts.add(MessageReceipt.builder()
					.message(saved)
					.user(member.getUser())
					.status(ReceiptStatus.SENT)
					.build());
		}
		receiptRepository.saveAll(receipts);

		conversation.setUpdatedAt(Instant.now());
		conversationRepository.save(conversation);

		return toResponse(saved, senderId, true);
	}

	@Transactional(readOnly = true)
	public List<MessageResponse> getHistory(Long conversationId, Long userId, Long beforeId, int size) {
		conversationService.requireMembership(conversationId, userId);
		int pageSize = Math.min(Math.max(size, 1), 100);
		List<Message> messages = messageRepository.findHistory(conversationId, beforeId, PageRequest.of(0, pageSize));
		return messages.stream()
				.sorted(Comparator.comparing(Message::getId))
				.map(m -> toResponse(m, userId, false))
				.toList();
	}

	@Transactional
	public MessageResponse markDelivered(Long messageId, Long userId) {
		return updateReceipt(messageId, userId, ReceiptStatus.DELIVERED);
	}

	@Transactional
	public MessageResponse markRead(Long messageId, Long userId) {
		MessageResponse response = updateReceipt(messageId, userId, ReceiptStatus.READ);
		Message message = messageRepository.findById(messageId)
				.orElseThrow(() -> new ResourceNotFoundException("Message not found"));
		memberRepository.findByConversationIdAndUserId(message.getConversation().getId(), userId)
				.ifPresent(member -> {
					member.setLastReadAt(Instant.now());
					memberRepository.save(member);
				});
		return response;
	}

	@Transactional
	public void markConversationRead(Long conversationId, Long userId, Long upToMessageId) {
		conversationService.requireMembership(conversationId, userId);
		List<Message> messages = messageRepository.findHistory(conversationId, null, PageRequest.of(0, 200));
		for (Message message : messages) {
			if (message.getSender().getId().equals(userId)) {
				continue;
			}
			if (upToMessageId != null && message.getId() > upToMessageId) {
				continue;
			}
			receiptRepository.findByMessageIdAndUserId(message.getId(), userId).ifPresent(receipt -> {
				if (receipt.getStatus() != ReceiptStatus.READ) {
					receipt.setStatus(ReceiptStatus.READ);
					receiptRepository.save(receipt);
				}
			});
		}
		memberRepository.findByConversationIdAndUserId(conversationId, userId).ifPresent(member -> {
			member.setLastReadAt(Instant.now());
			memberRepository.save(member);
		});
	}

	@Transactional(readOnly = true)
	public Message requireMessage(Long messageId) {
		return messageRepository.findByIdWithSender(messageId)
				.orElseThrow(() -> new ResourceNotFoundException("Message not found"));
	}

	private MessageResponse updateReceipt(Long messageId, Long userId, ReceiptStatus targetStatus) {
		Message message = messageRepository.findByIdWithSender(messageId)
				.orElseThrow(() -> new ResourceNotFoundException("Message not found"));
		if (message.getSender().getId().equals(userId)) {
			throw new BadRequestException("Sender cannot update receipt for own message");
		}
		conversationService.requireMembership(message.getConversation().getId(), userId);

		MessageReceipt receipt = receiptRepository.findByMessageIdAndUserId(messageId, userId)
				.orElseThrow(() -> new ForbiddenException("Receipt not found for this user"));

		if (isUpgrade(receipt.getStatus(), targetStatus)) {
			receipt.setStatus(targetStatus);
			receiptRepository.save(receipt);
		}
		return toResponse(message, message.getSender().getId(), true);
	}

	private boolean isUpgrade(ReceiptStatus current, ReceiptStatus target) {
		return target.ordinal() > current.ordinal();
	}

	private MessageResponse toResponse(Message message, Long viewerId, boolean includeReceipts) {
		ReceiptStatus myStatus = null;
		List<MessageResponse.ReceiptResponse> receipts = List.of();

		if (includeReceipts || !message.getSender().getId().equals(viewerId)) {
			List<MessageReceipt> all = receiptRepository.findWithUserByMessageId(message.getId());
			receipts = all.stream()
					.map(r -> MessageResponse.ReceiptResponse.builder()
							.userId(r.getUser().getId())
							.status(r.getStatus())
							.updatedAt(r.getUpdatedAt())
							.build())
					.toList();
			myStatus = all.stream()
					.filter(r -> r.getUser().getId().equals(viewerId))
					.map(MessageReceipt::getStatus)
					.findFirst()
					.orElse(null);
		}

		return MessageResponse.builder()
				.id(message.getId())
				.conversationId(message.getConversation().getId())
				.senderId(message.getSender().getId())
				.senderUsername(message.getSender().getUsername())
				.senderDisplayName(message.getSender().getDisplayName())
				.content(message.isDeleted() ? "" : message.getContent())
				.type(message.getType())
				.clientMessageId(message.getClientMessageId())
				.createdAt(message.getCreatedAt())
				.myReceiptStatus(myStatus)
				.receipts(includeReceipts ? receipts : null)
				.build();
	}

	@Transactional(readOnly = true)
	public MessageResponse getMessageResponse(Long messageId, Long viewerId) {
		Message message = requireMessage(messageId);
		conversationService.requireMembership(message.getConversation().getId(), viewerId);
		return toResponse(message, viewerId, true);
	}
}
