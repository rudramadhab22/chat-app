package com.rudra.ed.dto.response;

import com.rudra.ed.domain.enums.MessageType;
import com.rudra.ed.domain.enums.ReceiptStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class MessageResponse {

	private Long id;
	private Long conversationId;
	private Long senderId;
	private String senderUsername;
	private String senderDisplayName;
	private String content;
	private MessageType type;
	private String clientMessageId;
	private Instant createdAt;
	private ReceiptStatus myReceiptStatus;
	private List<ReceiptResponse> receipts;

	@Getter
	@Builder
	public static class ReceiptResponse {
		private Long userId;
		private ReceiptStatus status;
		private Instant updatedAt;
	}
}
