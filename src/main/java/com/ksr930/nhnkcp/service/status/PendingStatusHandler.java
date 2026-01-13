package com.ksr930.nhnkcp.service.status;

import com.ksr930.nhnkcp.domain.order.Order;
import com.ksr930.nhnkcp.domain.order.OrderStatus;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class PendingStatusHandler implements OrderStatusHandler {
	private static final Set<OrderStatus> ALLOWED = EnumSet.of(OrderStatus.RECEIVED, OrderStatus.CANCELED);

	@Override
	public OrderStatus status() {
		return OrderStatus.PENDING;
	}

	@Override
	public boolean canTransitionTo(OrderStatus to) {
		return ALLOWED.contains(to);
	}

	@Override
	public void onTransition(Order order, OrderStatus to) {
		// no-op
	}
}
