package com.ksr930.nhnkcp.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
	NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "요청한 자원을 찾을 수 없습니다."),
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청 값이 올바르지 않습니다."),
	OUT_OF_STOCK(HttpStatus.CONFLICT, "OUT_OF_STOCK", "재고가 부족합니다."),
	INVALID_STATUS_CHANGE(HttpStatus.CONFLICT, "INVALID_STATUS_CHANGE", "허용되지 않은 상태 변경입니다."),
	CONCURRENCY_FAILURE(HttpStatus.CONFLICT, "CONCURRENCY_FAILURE", "현재 주문량이 많아 잠시 후 다시 시도해주세요.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public HttpStatus getHttpStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}
