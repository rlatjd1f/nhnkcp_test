package com.ksr930.nhnkcp.dto.order;

public record OrderItemResponse(
		Long productId,
		String productName,
		int price,
		int quantity
) {
}
