package com.rudra.ed.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class CreateGroupConversationRequest {

	@NotBlank(message = "Group name is required")
	@Size(max = 120, message = "Group name must be at most 120 characters")
	private String name;

	@NotEmpty(message = "At least one member is required")
	private Set<Long> memberIds;
}
