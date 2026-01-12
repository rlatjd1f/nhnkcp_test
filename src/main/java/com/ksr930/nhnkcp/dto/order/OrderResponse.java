package com.ksr930.nhnkcp.dto.order;

import com.ksr930.nhnkcp.domain.order.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
		Long id,
		OrderStatus status,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		List<OrderItemResponse> items
) {
}
