package com.rudra.ed.controller;

import com.rudra.ed.config.openapi.OpenApiTags;
import com.rudra.ed.dto.request.LoginRequest;
import com.rudra.ed.dto.request.RegisterRequest;
import com.rudra.ed.dto.response.ApiResponse;
import com.rudra.ed.dto.response.AuthResponse;
import com.rudra.ed.dto.response.UserResponse;
import com.rudra.ed.service.AuthService;
import com.rudra.ed.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = OpenApiTags.AUTH, description = "Register, login, and current user profile")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	@SecurityRequirements
	@Operation(summary = "Register a new user", description = "Creates an account and returns a JWT access token.")
	public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		return ApiResponse.ok("Registered successfully", authService.register(request));
	}

	@PostMapping("/login")
	@SecurityRequirements
	@Operation(summary = "Login", description = "Authenticates with username/password and returns a JWT access token.")
	public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return ApiResponse.ok("Login successful", authService.login(request));
	}

	@GetMapping("/me")
	@SecurityRequirement(name = OpenApiTags.SECURITY_SCHEME_BEARER)
	@Operation(summary = "Current user", description = "Returns the authenticated user's profile.")
	public ApiResponse<UserResponse> me() {
		return ApiResponse.ok(authService.me(SecurityUtils.currentUserId()));
	}
}
