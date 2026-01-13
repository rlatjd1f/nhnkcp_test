package com.ksr930.nhnkcp.repository;

import com.ksr930.nhnkcp.domain.order.Order;
import com.ksr930.nhnkcp.domain.order.OrderStatus;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {
	Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);

	Page<Order> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

	@Query("select distinct o from Order o left join fetch o.items i left join fetch i.product where o.id = :id")
	java.util.Optional<Order> findByIdWithItems(@Param("id") Long id);
}
