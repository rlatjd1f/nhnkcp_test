package com.ksr930.nhnkcp.service;

import com.ksr930.nhnkcp.domain.order.OrderStatus;
import com.ksr930.nhnkcp.domain.order.Order;
import com.ksr930.nhnkcp.domain.order.OrderItem;
import com.ksr930.nhnkcp.domain.product.Product;
import com.ksr930.nhnkcp.dto.order.OrderCreateRequest;
import com.ksr930.nhnkcp.dto.order.OrderItemRequest;
import com.ksr930.nhnkcp.dto.order.OrderItemResponse;
import com.ksr930.nhnkcp.dto.order.OrderResponse;
import com.ksr930.nhnkcp.dto.order.OrderStatusUpdateRequest;
import com.ksr930.nhnkcp.exception.ApiException;
import com.ksr930.nhnkcp.exception.ErrorCode;
import com.ksr930.nhnkcp.repository.OrderRepository;
import com.ksr930.nhnkcp.service.status.OrderStatusHandler;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
	private final OrderRepository orderRepository;
	private final Map<OrderStatus, OrderStatusHandler> statusHandlers;
	private final StockService stockService;

	public OrderService(
			OrderRepository orderRepository,
			List<OrderStatusHandler> handlers,
			StockService stockService
	) {
		this.orderRepository = orderRepository;
		this.statusHandlers = handlers.stream()
				.collect(Collectors.toUnmodifiableMap(OrderStatusHandler::status, Function.identity()));
		this.stockService = stockService;
	}

	@Transactional
	public OrderResponse create(OrderCreateRequest request) {
		if (request.items() == null || request.items().isEmpty()) {
			throw new ApiException(ErrorCode.INVALID_REQUEST);
		}

		Map<Long, Integer> quantityByProductId = new HashMap<>();
		for (OrderItemRequest itemRequest : request.items()) {
			if (itemRequest.quantity() <= 0) {
				throw new ApiException(ErrorCode.INVALID_REQUEST);
			}
			quantityByProductId.merge(itemRequest.productId(), itemRequest.quantity(), Integer::sum);
		}

		Order order = new Order();
		List<Long> sortedProductIds = quantityByProductId.keySet()
				.stream()
				.sorted()
				.collect(Collectors.toList());

		for (Long productId : sortedProductIds) {
			Product product = stockService.getProduct(productId);
			int quantity = quantityByProductId.get(productId);
			if (product.getStockQuantity() < quantity) {
				throw new ApiException(ErrorCode.OUT_OF_STOCK);
			}
			OrderItem item = new OrderItem();
			item.setOrder(order);
			item.setProduct(product);
			item.setQuantity(quantity);
			item.setUnitPrice(item.getProduct().getPrice());
			order.getItems().add(item);
		}

		Order saved = orderRepository.save(order);
		return toResponse(saved);
	}

	@Transactional
	public OrderResponse updateStatus(Long id, OrderStatusUpdateRequest request) {
		if (request.status() == null) {
			throw new ApiException(ErrorCode.INVALID_REQUEST);
		}
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
		OrderStatus from = order.getStatus();
		OrderStatus to = request.status();

		OrderStatusHandler handler = statusHandlers.get(from);
		if (handler == null || !handler.canTransitionTo(to)) {
			throw new ApiException(ErrorCode.INVALID_STATUS_CHANGE);
		}

		handler.onTransition(order, to);

		order.setStatus(to);
		order.setUpdatedAt(LocalDateTime.now());
		return toResponse(order);
	}

	@Transactional(readOnly = true)
	public OrderResponse get(Long id) {
		Order order = orderRepository.findByIdWithItems(id)
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
		return toResponse(order);
	}

	@Transactional(readOnly = true)
	public Page<OrderResponse> list(Pageable pageable) {
		return orderRepository.findAllWithItems(pageable).map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public Page<OrderResponse> listByStatus(OrderStatus status, Pageable pageable) {
		return orderRepository.findAllByStatusWithItems(status, pageable).map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public Page<OrderResponse> listByPeriod(LocalDateTime start, LocalDateTime end, Pageable pageable) {
		return orderRepository.findAllByCreatedAtBetweenWithItems(start, end, pageable).map(this::toResponse);
	}

	private OrderResponse toResponse(Order order) {
		java.util.List<OrderItemResponse> items = new java.util.ArrayList<>();
		for (OrderItem item : order.getItems()) {
			OrderItemResponse itemResponse = new OrderItemResponse(
					item.getProduct().getId(),
					item.getProduct().getName(),
					item.getUnitPrice(),
					item.getQuantity()
			);
			items.add(itemResponse);
		}
		return new OrderResponse(
				order.getId(),
				order.getStatus(),
				order.getCreatedAt(),
				order.getUpdatedAt(),
				items
		);
	}
}
