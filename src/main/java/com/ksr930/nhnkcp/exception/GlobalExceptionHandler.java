package com.ksr930.nhnkcp.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		ErrorResponse response = new ErrorResponse(errorCode.getCode(), exception.getMessage());
		return ResponseEntity.status(errorCode.getStatus()).body(response);
	}
}
