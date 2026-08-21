package com.rudra.ed.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

	private String accessToken;
	private String tokenType;
	private UserResponse user;
}
