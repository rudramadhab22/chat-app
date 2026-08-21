package com.rudra.ed.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class UserResponse {

	private Long id;
	private String username;
	private String email;
	private String displayName;
	private String profilePictureUrl;
	private String about;
	private boolean online;
	private Instant lastSeenAt;
	private Instant createdAt;
}
