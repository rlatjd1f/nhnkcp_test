package com.ksr930.nhnkcp.service.status;

import com.ksr930.nhnkcp.domain.order.Order;
import com.ksr930.nhnkcp.domain.order.OrderStatus;

public interface OrderStatusHandler {
	OrderStatus status();

	boolean canTransitionTo(OrderStatus to);

	void onTransition(Order order, OrderStatus to);
}
