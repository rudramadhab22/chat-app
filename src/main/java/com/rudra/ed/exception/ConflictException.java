package com.rudra.ed.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {

	public ConflictException(String message) {
		super(message, HttpStatus.CONFLICT);
	}
}
