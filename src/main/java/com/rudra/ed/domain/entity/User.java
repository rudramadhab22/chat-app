package com.rudra.ed.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
		name = "users",
		indexes = {
				@Index(name = "idx_users_username", columnList = "username"),
				@Index(name = "idx_users_email", columnList = "email")
		}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 50)
	private String username;

	@Column(nullable = false, unique = true, length = 120)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(name = "display_name", nullable = false, length = 100)
	private String displayName;

	@Column(name = "profile_picture_url", length = 500)
	private String profilePictureUrl;

	@Column(length = 255)
	private String about;

	@Column(nullable = false)
	@Builder.Default
	private boolean online = false;

	@Column(name = "last_seen_at")
	private Instant lastSeenAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
		if (lastSeenAt == null) {
			lastSeenAt = now;
		}
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}
}
