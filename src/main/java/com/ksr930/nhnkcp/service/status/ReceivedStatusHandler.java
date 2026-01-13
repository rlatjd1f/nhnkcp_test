package com.ksr930.nhnkcp.service.status;

import com.ksr930.nhnkcp.domain.order.Order;
import com.ksr930.nhnkcp.domain.order.OrderStatus;
import com.ksr930.nhnkcp.service.StockService;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ReceivedStatusHandler implements OrderStatusHandler {
	private static final Set<OrderStatus> ALLOWED = EnumSet.of(OrderStatus.COMPLETED, OrderStatus.CANCELED);
	private final StockService stockService;

	public ReceivedStatusHandler(StockService stockService) {
		this.stockService = stockService;
	}

	@Override
	public OrderStatus status() {
		return OrderStatus.RECEIVED;
	}

	@Override
	public boolean canTransitionTo(OrderStatus to) {
		return ALLOWED.contains(to);
	}

	@Override
	public void onTransition(Order order, OrderStatus to) {
		if (to == OrderStatus.COMPLETED) {
			stockService.decrease(order.getItems());
		}
	}
}
