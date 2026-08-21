package com.rudra.ed.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessageRequest {

	@NotBlank(message = "content is required")
	@Size(max = 4000, message = "content must be at most 4000 characters")
	private String content;

	@Size(max = 64, message = "clientMessageId must be at most 64 characters")
	private String clientMessageId;
}
