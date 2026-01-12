package com.ksr930.nhnkcp.repository;

import com.ksr930.nhnkcp.domain.order.Order;
import com.ksr930.nhnkcp.domain.order.OrderStatus;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
	Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);

	Page<Order> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
