package com.rudra.ed.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class AddMembersRequest {

	@NotEmpty(message = "memberIds must not be empty")
	private Set<Long> memberIds;
}
