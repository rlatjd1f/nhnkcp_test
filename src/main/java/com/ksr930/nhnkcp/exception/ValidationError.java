package com.ksr930.nhnkcp.exception;

public record ValidationError(
		String field,
		String reason
) {
}
