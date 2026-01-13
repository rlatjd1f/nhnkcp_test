package com.ksr930.nhnkcp.exception;

import com.ksr930.nhnkcp.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		ApiResponse<Void> response = ApiResponse.error(errorCode, exception.getMessage(), null);
		return ResponseEntity.status(errorCode.getStatus()).body(response);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<List<ValidationError>>> handleMethodArgumentNotValid(
			MethodArgumentNotValidException exception
	) {
		List<ValidationError> errors = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(this::toValidationError)
				.collect(Collectors.toList());
		ApiResponse<List<ValidationError>> response = ApiResponse.error(
				ErrorCode.INVALID_REQUEST,
				ErrorCode.INVALID_REQUEST.getMessage(),
				errors
		);
		return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatus()).body(response);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<List<ValidationError>>> handleConstraintViolation(
			ConstraintViolationException exception
	) {
		List<ValidationError> errors = exception.getConstraintViolations()
				.stream()
				.map(violation -> new ValidationError(
						violation.getPropertyPath().toString(),
						violation.getMessage()
				))
				.collect(Collectors.toList());
		ApiResponse<List<ValidationError>> response = ApiResponse.error(
				ErrorCode.INVALID_REQUEST,
				ErrorCode.INVALID_REQUEST.getMessage(),
				errors
		);
		return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatus()).body(response);
	}

	@ExceptionHandler(PessimisticLockingFailureException.class)
	public ResponseEntity<ApiResponse<Void>> handleLockFailure(PessimisticLockingFailureException exception) {
		ApiResponse<Void> response = ApiResponse.error(ErrorCode.CONCURRENCY_FAILURE, null);
		return ResponseEntity.status(ErrorCode.CONCURRENCY_FAILURE.getStatus()).body(response);
	}

	private ValidationError toValidationError(FieldError error) {
		String field = error.getField();
		String reason = error.getDefaultMessage() == null ? "invalid" : error.getDefaultMessage();
		return new ValidationError(field, reason);
	}
}
