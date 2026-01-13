package com.ksr930.nhnkcp.exception;

import lombok.Getter;
@Getter
public class ApiException extends RuntimeException {
	private final ErrorCode errorCode;

	public ApiException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}
}
