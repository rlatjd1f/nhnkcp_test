package com.ksr930.nhnkcp.exception;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class ErrorResponse {
	private final String code;
	private final String message;
	private final List<ValidationError> errors;
	private final LocalDateTime timestamp = LocalDateTime.now();

	public ErrorResponse(String code, String message) {
		this(code, message, null);
	}

	public ErrorResponse(String code, String message, List<ValidationError> errors) {
		this.code = code;
		this.message = message;
		this.errors = errors;
	}

	@Getter
	public static class ValidationError {
		private final String field;
		private final String reason;

		public ValidationError(String field, String reason) {
			this.field = field;
			this.reason = reason;
		}
	}
}
