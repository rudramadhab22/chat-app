package com.rudra.ed.controller;

import com.rudra.ed.config.openapi.OpenApiTags;
import com.rudra.ed.dto.request.AddMembersRequest;
import com.rudra.ed.dto.request.CreateDirectConversationRequest;
import com.rudra.ed.dto.request.CreateGroupConversationRequest;
import com.rudra.ed.dto.response.ApiResponse;
import com.rudra.ed.dto.response.ConversationResponse;
import com.rudra.ed.service.ConversationService;
import com.rudra.ed.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Tag(name = OpenApiTags.CONVERSATIONS, description = "Direct chats, groups, and membership")
@SecurityRequirement(name = OpenApiTags.SECURITY_SCHEME_BEARER)
public class ConversationController {

	private final ConversationService conversationService;

	@PostMapping("/direct")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create or get direct chat", description = "Idempotent for a given user pair.")
	public ApiResponse<ConversationResponse> createDirect(@Valid @RequestBody CreateDirectConversationRequest request) {
		return ApiResponse.ok("Direct conversation ready",
				conversationService.createDirect(SecurityUtils.currentUserId(), request));
	}

	@PostMapping("/groups")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create group conversation", description = "Caller becomes ADMIN; memberIds must include at least one other user.")
	public ApiResponse<ConversationResponse> createGroup(@Valid @RequestBody CreateGroupConversationRequest request) {
		return ApiResponse.ok("Group created",
				conversationService.createGroup(SecurityUtils.currentUserId(), request));
	}

	@GetMapping
	@Operation(summary = "List my conversations", description = "Ordered by most recently updated.")
	public ApiResponse<List<ConversationResponse>> list() {
		return ApiResponse.ok(conversationService.listForUser(SecurityUtils.currentUserId()));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get conversation detail")
	public ApiResponse<ConversationResponse> get(@PathVariable Long id) {
		return ApiResponse.ok(conversationService.getById(id, SecurityUtils.currentUserId()));
	}

	@PostMapping("/{id}/members")
	@Operation(summary = "Add group members", description = "Admin only.")
	public ApiResponse<ConversationResponse> addMembers(
			@PathVariable Long id,
			@Valid @RequestBody AddMembersRequest request
	) {
		return ApiResponse.ok("Members added",
				conversationService.addMembers(id, SecurityUtils.currentUserId(), request));
	}

	@DeleteMapping("/{id}/members/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Remove member or leave group", description = "Members may leave themselves; admins may remove others.")
	public void removeMember(@PathVariable Long id, @PathVariable Long userId) {
		conversationService.removeMember(id, SecurityUtils.currentUserId(), userId);
	}
}
