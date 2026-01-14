package com.ksr930.nhnkcp.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ksr930.nhnkcp.domain.order.OrderStatus;
import com.ksr930.nhnkcp.domain.product.Category;
import com.ksr930.nhnkcp.domain.product.Product;
import com.ksr930.nhnkcp.dto.order.OrderCreateRequest;
import com.ksr930.nhnkcp.dto.order.OrderItemRequest;
import com.ksr930.nhnkcp.dto.order.OrderResponse;
import com.ksr930.nhnkcp.dto.order.OrderStatusUpdateRequest;
import com.ksr930.nhnkcp.exception.ApiException;
import com.ksr930.nhnkcp.exception.ErrorCode;
import com.ksr930.nhnkcp.repository.OrderRepository;
import com.ksr930.nhnkcp.repository.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.PessimisticLockingFailureException;

@SpringBootTest
class OrderServiceConcurrencyTests {
    @Autowired
    private OrderService orderService;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private OrderRepository orderRepository;

	@BeforeEach
	void clearData() {
		orderRepository.deleteAll();
		productRepository.deleteAll();
	}

	@Test
	@DisplayName("동시에 완료 처리를 해도 재고가 정확히 차감된다")
	void 동시에_완료_처리를_해도_재고가_정확히_차감된다() throws Exception {
		// 테스트 대상: OrderService#updateStatus (동시 완료 처리), 의도: 재고 차감과 상태 전이가 원자적으로 동작하는지 검증
		Product product = createProduct("동시성 테스트", 3000, 3);
		OrderResponse order1 = orderService.create(createOrderRequest(product.getId(), 2));
		OrderResponse order2 = orderService.create(createOrderRequest(product.getId(), 2));

		// 준비 단계: 두 주문을 모두 RECEIVED로 올려 완료 전이 조건을 맞춘다.
		orderService.updateStatus(order1.id(), new OrderStatusUpdateRequest(OrderStatus.RECEIVED));
		orderService.updateStatus(order2.id(), new OrderStatusUpdateRequest(OrderStatus.RECEIVED));

		// 동시성 시뮬레이션: 동일 시점에 COMPLETED 전이를 시도하도록 시작 신호를 맞춘다.
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		List<Callable<Throwable>> tasks = List.of(
				() -> runCompletion(start, order1.id()),
				() -> runCompletion(start, order2.id())
        );

        List<Future<Throwable>> futures = new ArrayList<>();
        for (Callable<Throwable> task : tasks) {
            futures.add(executor.submit(task));
        }

        start.countDown();

        List<Throwable> failures = new ArrayList<>();
        for (Future<Throwable> future : futures) {
            Throwable failure = future.get();
            if (failure != null) {
                failures.add(failure);
            }
        }

        executor.shutdown();

		// 기대 결과: 재고는 정확히 2개만 차감되어 1이 남아야 한다.
		Product updated = productRepository.findById(product.getId()).orElseThrow();
		assertThat(updated.getStockQuantity()).isEqualTo(1);

		// 기대 결과: 두 주문 중 하나만 COMPLETED, 나머지는 실패해야 한다.
		OrderResponse updatedOrder1 = orderService.get(order1.id());
		OrderResponse updatedOrder2 = orderService.get(order2.id());
        long completedCount = List.of(updatedOrder1, updatedOrder2).stream()
                .filter(order -> order.status() == OrderStatus.COMPLETED)
                .count();

        assertThat(completedCount).isEqualTo(1);
        assertThat(failures).hasSize(1);
        assertThat(isExpectedFailure(failures.get(0))).isTrue();
    }

    private Throwable runCompletion(CountDownLatch start, Long orderId) {
        try {
            start.await();
            orderService.updateStatus(orderId, new OrderStatusUpdateRequest(OrderStatus.COMPLETED));
            return null;
        } catch (Throwable ex) {
            return ex;
        }
    }

    private boolean isExpectedFailure(Throwable failure) {
        if (failure instanceof ApiException apiException) {
            return apiException.getErrorCode() == ErrorCode.OUT_OF_STOCK;
        }
        return failure instanceof PessimisticLockingFailureException;
    }

    private Product createProduct(String name, int price, int stock) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setCategory(Category.BEVERAGE);
        return productRepository.save(product);
    }

    private OrderCreateRequest createOrderRequest(Long productId, int quantity) {
        OrderItemRequest item = new OrderItemRequest(productId, quantity);
        return new OrderCreateRequest(List.of(item));
    }
}
