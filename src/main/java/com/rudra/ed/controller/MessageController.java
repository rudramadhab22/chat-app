package com.rudra.ed.controller;

import com.rudra.ed.config.openapi.OpenApiTags;
import com.rudra.ed.dto.request.SendMessageRequest;
import com.rudra.ed.dto.response.ApiResponse;
import com.rudra.ed.dto.response.MessageResponse;
import com.rudra.ed.service.ConversationService;
import com.rudra.ed.service.MessageService;
import com.rudra.ed.service.PresenceService;
import com.rudra.ed.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conversations/{conversationId}/messages")
@RequiredArgsConstructor
@Tag(name = OpenApiTags.MESSAGES, description = "Chat history and REST message send")
@SecurityRequirement(name = OpenApiTags.SECURITY_SCHEME_BEARER)
public class MessageController {

	private final MessageService messageService;
	private final ConversationService conversationService;
	private final PresenceService presenceService;

	@GetMapping
	@Operation(
			summary = "Message history",
			description = "Cursor pagination: pass beforeId to load older messages. Returned oldest → newest."
	)
	public ApiResponse<List<MessageResponse>> history(
			@PathVariable Long conversationId,
			@Parameter(description = "Load messages with id strictly less than this value")
			@RequestParam(required = false) Long beforeId,
			@Parameter(description = "Page size (1–100)", example = "50")
			@RequestParam(defaultValue = "50") int size
	) {
		return ApiResponse.ok(messageService.getHistory(conversationId, SecurityUtils.currentUserId(), beforeId, size));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
			summary = "Send message (REST fallback)",
			description = "Persists the message and pushes CHAT_MESSAGE over WebSocket to online members."
	)
	public ApiResponse<MessageResponse> send(
			@PathVariable Long conversationId,
			@Valid @RequestBody SendMessageRequest request
	) {
		MessageResponse saved = messageService.sendMessage(conversationId, SecurityUtils.currentUserId(), request);
		presenceService.pushMessageToConversationMembers(
				conversationService.memberUserIds(conversationId),
				saved
		);
		return ApiResponse.ok("Message sent", saved);
	}
}
