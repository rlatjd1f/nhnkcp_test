package com.ksr930.nhnkcp.service.status;

import com.ksr930.nhnkcp.domain.order.Order;
import com.ksr930.nhnkcp.domain.order.OrderStatus;
import org.springframework.stereotype.Component;

@Component
public class CanceledStatusHandler implements OrderStatusHandler {
	@Override
	public OrderStatus status() {
		return OrderStatus.CANCELED;
	}

	@Override
	public boolean canTransitionTo(OrderStatus to) {
		return false;
	}

	@Override
	public void onTransition(Order order, OrderStatus to) {
		// no-op
	}
}
