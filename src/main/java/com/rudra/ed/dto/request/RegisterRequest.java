package com.rudra.ed.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

	@NotBlank(message = "Username is required")
	@Size(min = 3, max = 50, message = "Username must be 3-50 characters")
	private String username;

	@NotBlank(message = "Email is required")
	@Email(message = "Email must be valid")
	private String email;

	@NotBlank(message = "Password is required")
	@Size(min = 6, max = 100, message = "Password must be at least 6 characters")
	private String password;

	@NotBlank(message = "Display name is required")
	@Size(max = 100, message = "Display name must be at most 100 characters")
	private String displayName;
}
