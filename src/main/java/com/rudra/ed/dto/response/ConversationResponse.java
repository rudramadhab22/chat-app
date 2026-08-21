package com.rudra.ed.dto.response;

import com.rudra.ed.domain.enums.ConversationType;
import com.rudra.ed.domain.enums.MemberRole;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class ConversationResponse {

	private Long id;
	private ConversationType type;
	private String name;
	private String avatarUrl;
	private Long createdById;
	private Instant createdAt;
	private Instant updatedAt;
	private MessageResponse lastMessage;
	private List<MemberResponse> members;

	@Getter
	@Builder
	public static class MemberResponse {
		private Long userId;
		private String username;
		private String displayName;
		private String profilePictureUrl;
		private MemberRole role;
		private boolean online;
		private Instant lastSeenAt;
	}
}
