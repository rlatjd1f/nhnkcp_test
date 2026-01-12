package com.ksr930.nhnkcp.dto.order;

import com.ksr930.nhnkcp.domain.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(
		@NotNull OrderStatus status
) {
}
