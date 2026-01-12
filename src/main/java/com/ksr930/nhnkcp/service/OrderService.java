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
import com.ksr930.nhnkcp.repository.ProductRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;

	public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
		this.orderRepository = orderRepository;
		this.productRepository = productRepository;
	}

	@Transactional
	public OrderResponse create(OrderCreateRequest request) {
		if (request.items() == null || request.items().isEmpty()) {
			throw new ApiException(ErrorCode.INVALID_REQUEST, "주문 상품이 비어 있습니다.");
		}

		Order order = new Order();
		for (OrderItemRequest itemRequest : request.items()) {
			if (itemRequest.quantity() <= 0) {
				throw new ApiException(ErrorCode.INVALID_REQUEST, "주문 수량은 1 이상이어야 합니다.");
			}
			Product product = productRepository.findByIdForUpdate(itemRequest.productId())
					.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
			if (product.getStockQuantity() < itemRequest.quantity()) {
				throw new ApiException(ErrorCode.OUT_OF_STOCK, "재고가 부족합니다.");
			}
			OrderItem item = new OrderItem();
			item.setOrder(order);
			item.setProduct(product);
			item.setQuantity(itemRequest.quantity());
			item.setUnitPrice(product.getPrice());
			order.getItems().add(item);
		}

		Order saved = orderRepository.save(order);
		return toResponse(saved);
	}

	@Transactional
	public OrderResponse updateStatus(Long id, OrderStatusUpdateRequest request) {
		if (request.status() == null) {
			throw new ApiException(ErrorCode.INVALID_REQUEST, "변경할 주문 상태가 필요합니다.");
		}
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "주문을 찾을 수 없습니다."));
		OrderStatus from = order.getStatus();
		OrderStatus to = request.status();

		if (!isValidTransition(from, to)) {
			throw new ApiException(ErrorCode.INVALID_STATUS_CHANGE, "허용되지 않은 상태 변경입니다.");
		}

		if (to == OrderStatus.COMPLETED) {
			applyStockDecrease(order.getItems());
		}
		if (to == OrderStatus.CANCELED && from == OrderStatus.COMPLETED) {
			applyStockRestore(order.getItems());
		}

		order.setStatus(to);
		order.setUpdatedAt(LocalDateTime.now());
		return toResponse(order);
	}

	@Transactional(readOnly = true)
	public OrderResponse get(Long id) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "주문을 찾을 수 없습니다."));
		return toResponse(order);
	}

	@Transactional(readOnly = true)
	public Page<OrderResponse> list(Pageable pageable) {
		return orderRepository.findAll(pageable).map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public Page<OrderResponse> listByStatus(OrderStatus status, Pageable pageable) {
		return orderRepository.findAllByStatus(status, pageable).map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public Page<OrderResponse> listByPeriod(LocalDateTime start, LocalDateTime end, Pageable pageable) {
		return orderRepository.findAllByCreatedAtBetween(start, end, pageable).map(this::toResponse);
	}

	private void applyStockDecrease(List<OrderItem> items) {
		for (OrderItem item : items) {
			Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
					.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
			if (product.getStockQuantity() < item.getQuantity()) {
				throw new ApiException(ErrorCode.OUT_OF_STOCK, "재고가 부족합니다.");
			}
			product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
		}
	}

	private void applyStockRestore(List<OrderItem> items) {
		for (OrderItem item : items) {
			Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
					.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
			product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
		}
	}

	private boolean isValidTransition(OrderStatus from, OrderStatus to) {
		return switch (from) {
			case PENDING -> to == OrderStatus.RECEIVED || to == OrderStatus.CANCELED;
			case RECEIVED -> to == OrderStatus.COMPLETED || to == OrderStatus.CANCELED;
			case COMPLETED -> to == OrderStatus.CANCELED;
			case CANCELED -> false;
		};
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
