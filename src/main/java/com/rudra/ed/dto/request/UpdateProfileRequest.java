package com.rudra.ed.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

	@Size(max = 100, message = "Display name must be at most 100 characters")
	private String displayName;

	@Size(max = 255, message = "About must be at most 255 characters")
	private String about;
}
