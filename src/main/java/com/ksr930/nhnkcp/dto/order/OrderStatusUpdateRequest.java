package com.ksr930.nhnkcp.dto.order;

import com.ksr930.nhnkcp.domain.order.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderStatusUpdateRequest {
	@NotNull
	private OrderStatus status;
}
