package com.rudra.ed.dto.mapper;

import com.rudra.ed.domain.entity.User;
import com.rudra.ed.dto.response.UserResponse;

public final class UserMapper {

	private UserMapper() {
	}

	public static UserResponse toResponse(User user) {
		return UserResponse.builder()
				.id(user.getId())
				.username(user.getUsername())
				.email(user.getEmail())
				.displayName(user.getDisplayName())
				.profilePictureUrl(user.getProfilePictureUrl())
				.about(user.getAbout())
				.online(user.isOnline())
				.lastSeenAt(user.getLastSeenAt())
				.createdAt(user.getCreatedAt())
				.build();
	}
}
