package com.ksr930.nhnkcp.dto;

import com.ksr930.nhnkcp.exception.ErrorCode;

public record ApiResponse<T>(
		int status,
		String code,
		String message,
		T data
) {
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(200, "OK", "성공", data);
	}

	public static <T> ApiResponse<T> success(int status, T data) {
		return new ApiResponse<>(status, "OK", "성공", data);
	}

	public static <T> ApiResponse<T> error(ErrorCode errorCode, T data) {
		return new ApiResponse<>(
				errorCode.getHttpStatus().value(),
				errorCode.getCode(),
				errorCode.getMessage(),
				data
		);
	}

	public static <T> ApiResponse<T> error(ErrorCode errorCode, String message, T data) {
		return new ApiResponse<>(
				errorCode.getHttpStatus().value(),
				errorCode.getCode(),
				message,
				data
		);
	}
}
