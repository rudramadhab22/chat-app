package com.rudra.ed.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

	private final boolean success;
	private final String message;
	private final T data;
	private final Instant timestamp;

	public static <T> ApiResponse<T> ok(T data) {
		return ApiResponse.<T>builder()
				.success(true)
				.message("OK")
				.data(data)
				.timestamp(Instant.now())
				.build();
	}

	public static <T> ApiResponse<T> ok(String message, T data) {
		return ApiResponse.<T>builder()
				.success(true)
				.message(message)
				.data(data)
				.timestamp(Instant.now())
				.build();
	}

	public static ApiResponse<Void> okMessage(String message) {
		return ApiResponse.<Void>builder()
				.success(true)
				.message(message)
				.timestamp(Instant.now())
				.build();
	}

	public static ApiResponse<Void> error(String message) {
		return ApiResponse.<Void>builder()
				.success(false)
				.message(message)
				.timestamp(Instant.now())
				.build();
	}
}
