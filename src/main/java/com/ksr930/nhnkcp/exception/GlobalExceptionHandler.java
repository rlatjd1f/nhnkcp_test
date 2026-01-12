package com.ksr930.nhnkcp.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
		List<ErrorResponse.ValidationError> errors = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(this::toValidationError)
				.collect(Collectors.toList());
		ErrorResponse response = new ErrorResponse(
				ErrorCode.INVALID_REQUEST.getCode(),
				"요청 값이 올바르지 않습니다.",
				errors
		);
		return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatus()).body(response);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
		List<ErrorResponse.ValidationError> errors = exception.getConstraintViolations()
				.stream()
				.map(violation -> new ErrorResponse.ValidationError(
						violation.getPropertyPath().toString(),
						violation.getMessage()
				))
				.collect(Collectors.toList());
		ErrorResponse response = new ErrorResponse(
				ErrorCode.INVALID_REQUEST.getCode(),
				"요청 값이 올바르지 않습니다.",
				errors
		);
		return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatus()).body(response);
	}

	private ErrorResponse.ValidationError toValidationError(FieldError error) {
		String field = error.getField();
		String reason = error.getDefaultMessage() == null ? "invalid" : error.getDefaultMessage();
		return new ErrorResponse.ValidationError(field, reason);
	}
}
