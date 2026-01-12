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
	@Query(
			value = "select distinct o from Order o left join fetch o.items i left join fetch i.product",
			countQuery = "select count(o) from Order o"
	)
	Page<Order> findAllWithItems(Pageable pageable);

	@Query(
			value = "select distinct o from Order o left join fetch o.items i left join fetch i.product " +
					"where o.status = :status",
			countQuery = "select count(o) from Order o where o.status = :status"
	)
	Page<Order> findAllByStatusWithItems(@Param("status") OrderStatus status, Pageable pageable);

	@Query(
			value = "select distinct o from Order o left join fetch o.items i left join fetch i.product " +
					"where o.createdAt between :start and :end",
			countQuery = "select count(o) from Order o where o.createdAt between :start and :end"
	)
	Page<Order> findAllByCreatedAtBetweenWithItems(
			@Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end,
			Pageable pageable
	);
}
