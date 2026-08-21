package com.rudra.ed.controller;

import com.rudra.ed.config.openapi.OpenApiTags;
import com.rudra.ed.dto.request.UpdateProfileRequest;
import com.rudra.ed.dto.response.ApiResponse;
import com.rudra.ed.dto.response.UserResponse;
import com.rudra.ed.service.UserService;
import com.rudra.ed.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = OpenApiTags.USERS, description = "User search, profile updates, and avatars")
@SecurityRequirement(name = OpenApiTags.SECURITY_SCHEME_BEARER)
public class UserController {

	private final UserService userService;

	@GetMapping("/search")
	@Operation(summary = "Search users", description = "Search by username, display name, or email (min 2 characters).")
	public ApiResponse<List<UserResponse>> search(
			@Parameter(description = "Search query", example = "alice")
			@RequestParam("q") String query
	) {
		return ApiResponse.ok(userService.search(query));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get user by id")
	public ApiResponse<UserResponse> getById(@PathVariable Long id) {
		return ApiResponse.ok(userService.getById(id));
	}

	@PutMapping("/me")
	@Operation(summary = "Update my profile", description = "Updates display name and/or about text.")
	public ApiResponse<UserResponse> updateMe(@Valid @RequestBody UpdateProfileRequest request) {
		return ApiResponse.ok("Profile updated", userService.updateProfile(SecurityUtils.currentUserId(), request));
	}

	@PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Upload avatar", description = "Accepts JPEG, PNG, WEBP, or GIF up to 5MB.")
	public ApiResponse<UserResponse> uploadAvatar(@RequestPart("file") MultipartFile file) {
		return ApiResponse.ok("Avatar updated", userService.updateAvatar(SecurityUtils.currentUserId(), file));
	}
}
