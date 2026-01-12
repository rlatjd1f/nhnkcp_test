package com.ksr930.nhnkcp.dto.order;

import com.ksr930.nhnkcp.domain.order.OrderStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderResponse {
	private Long id;
	private OrderStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<OrderItemResponse> items = new ArrayList<>();
}
