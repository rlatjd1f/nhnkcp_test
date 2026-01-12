package com.ksr930.nhnkcp.controller;

import com.ksr930.nhnkcp.domain.order.OrderStatus;
import com.ksr930.nhnkcp.dto.order.OrderCreateRequest;
import com.ksr930.nhnkcp.dto.order.OrderResponse;
import com.ksr930.nhnkcp.dto.order.OrderStatusUpdateRequest;
import com.ksr930.nhnkcp.service.OrderService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request) {
		return ResponseEntity.ok(orderService.create(request));
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<OrderResponse> updateStatus(
			@PathVariable Long id,
			@Valid @RequestBody OrderStatusUpdateRequest request
	) {
		return ResponseEntity.ok(orderService.updateStatus(id, request));
	}

	@GetMapping("/{id}")
	public ResponseEntity<OrderResponse> get(@PathVariable Long id) {
		return ResponseEntity.ok(orderService.get(id));
	}

	@GetMapping
	public ResponseEntity<Page<OrderResponse>> list(
			@RequestParam(required = false) OrderStatus status,
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
			Pageable pageable
	) {
		if (status != null) {
			return ResponseEntity.ok(orderService.listByStatus(status, pageable));
		}
		if (start != null && end != null) {
			return ResponseEntity.ok(orderService.listByPeriod(start, end, pageable));
		}
		return ResponseEntity.ok(orderService.list(pageable));
	}
}
