package com.rudra.ed.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDirectConversationRequest {

	@NotNull(message = "otherUserId is required")
	private Long otherUserId;
}
