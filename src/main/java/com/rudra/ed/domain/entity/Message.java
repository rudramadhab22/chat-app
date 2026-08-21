package com.rudra.ed.domain.entity;

import com.rudra.ed.domain.enums.MessageType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
		name = "messages",
		indexes = {
				@Index(name = "idx_messages_conversation_id", columnList = "conversation_id"),
				@Index(name = "idx_messages_created_at", columnList = "created_at")
		}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "conversation_id", nullable = false)
	private Conversation conversation;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "sender_id", nullable = false)
	private User sender;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@Builder.Default
	private MessageType type = MessageType.TEXT;

	@Column(name = "client_message_id", length = 64)
	private String clientMessageId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	@Builder.Default
	private boolean deleted = false;

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}
}
