package com.ksr930.nhnkcp.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
	NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND"),
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST"),
	OUT_OF_STOCK(HttpStatus.CONFLICT, "OUT_OF_STOCK"),
	INVALID_STATUS_CHANGE(HttpStatus.CONFLICT, "INVALID_STATUS_CHANGE");

	private final HttpStatus status;
	private final String code;

	ErrorCode(HttpStatus status, String code) {
		this.status = status;
		this.code = code;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}
}
