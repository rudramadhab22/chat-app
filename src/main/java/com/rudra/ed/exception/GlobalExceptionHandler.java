package com.rudra.ed.exception;

import com.rudra.ed.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
		return ResponseEntity.status(ex.getStatus()).body(ApiResponse.error(ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.joining("; "));
		return ResponseEntity.badRequest().body(ApiResponse.error(message));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access denied"));
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(ApiResponse.error("Invalid username or password"));
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ApiResponse<Void>> handleMaxUpload(MaxUploadSizeExceededException ex) {
		return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
				.body(ApiResponse.error("File size exceeds the allowed limit"));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.error("Unexpected server error: " + ex.getMessage()));
	}
}
