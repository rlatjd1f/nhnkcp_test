package com.ksr930.nhnkcp.exception;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ErrorResponse {
	private final String code;
	private final String message;
	private final LocalDateTime timestamp = LocalDateTime.now();

	public ErrorResponse(String code, String message) {
		this.code = code;
		this.message = message;
	}
}
