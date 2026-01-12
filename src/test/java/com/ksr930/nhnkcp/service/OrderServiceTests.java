package com.ksr930.nhnkcp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ksr930.nhnkcp.domain.order.OrderStatus;
import com.ksr930.nhnkcp.domain.product.Category;
import com.ksr930.nhnkcp.domain.product.Product;
import com.ksr930.nhnkcp.dto.order.OrderCreateRequest;
import com.ksr930.nhnkcp.dto.order.OrderItemRequest;
import com.ksr930.nhnkcp.dto.order.OrderResponse;
import com.ksr930.nhnkcp.dto.order.OrderStatusUpdateRequest;
import com.ksr930.nhnkcp.exception.ApiException;
import com.ksr930.nhnkcp.repository.ProductRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class OrderServiceTests {
	@Autowired
	private OrderService orderService;

	@Autowired
	private ProductRepository productRepository;

	@Test
	@DisplayName("주문 완료 시 재고가 차감된다")
	void 주문_완료_시_재고가_차감된다() {
		Product product = createProduct("콜라", 1000, 5);
		OrderResponse order = orderService.create(createOrderRequest(product.getId(), 2));

		orderService.updateStatus(order.id(), new OrderStatusUpdateRequest(OrderStatus.RECEIVED));

		orderService.updateStatus(order.id(), new OrderStatusUpdateRequest(OrderStatus.COMPLETED));

		Product updated = productRepository.findById(product.getId()).orElseThrow();
		assertThat(updated.getStockQuantity()).isEqualTo(3);
	}

	@Test
	@DisplayName("완료된 주문을 취소하면 재고가 복구된다")
	void 완료된_주문을_취소하면_재고가_복구된다() {
		Product product = createProduct("커피", 2000, 3);
		OrderResponse order = orderService.create(createOrderRequest(product.getId(), 2));

		orderService.updateStatus(order.id(), new OrderStatusUpdateRequest(OrderStatus.RECEIVED));
		orderService.updateStatus(order.id(), new OrderStatusUpdateRequest(OrderStatus.COMPLETED));
		orderService.updateStatus(order.id(), new OrderStatusUpdateRequest(OrderStatus.CANCELED));

		Product updated = productRepository.findById(product.getId()).orElseThrow();
		assertThat(updated.getStockQuantity()).isEqualTo(3);
	}

	@Test
	@DisplayName("재고 부족 시 주문 생성이 실패한다")
	void 재고가_부족하면_주문_생성이_실패한다() {
		Product product = createProduct("샌드위치", 5000, 1);

		assertThatThrownBy(() -> orderService.create(createOrderRequest(product.getId(), 2)))
				.isInstanceOf(ApiException.class);
	}

	@Test
	@DisplayName("잘못된 상태 변경은 실패한다")
	void 잘못된_상태_변경은_실패한다() {
		Product product = createProduct("케이크", 12000, 2);
		OrderResponse order = orderService.create(createOrderRequest(product.getId(), 1));

		assertThatThrownBy(() -> orderService.updateStatus(
				order.id(),
				new OrderStatusUpdateRequest(OrderStatus.COMPLETED)
		))
				.isInstanceOf(ApiException.class);
	}

	@Test
	@DisplayName("상태별 주문 목록을 조회한다")
	void 상태별_주문_목록을_조회한다() {
		Product product = createProduct("도넛", 3000, 5);
		orderService.create(createOrderRequest(product.getId(), 1));

		OrderResponse receivedOrder = orderService.create(createOrderRequest(product.getId(), 1));
		orderService.updateStatus(receivedOrder.id(), new OrderStatusUpdateRequest(OrderStatus.RECEIVED));

		Page<OrderResponse> responses =
				orderService.listByStatus(OrderStatus.RECEIVED, PageRequest.of(0, 10));

		assertThat(responses.getContent()).hasSize(1);
		assertThat(responses.getContent().get(0).id()).isEqualTo(receivedOrder.id());
	}

	@Test
	@DisplayName("기간별 주문 목록을 조회한다")
	void 기간별_주문_목록을_조회한다() {
		Product product = createProduct("샐러드", 8000, 5);
		orderService.create(createOrderRequest(product.getId(), 1));

		LocalDateTime start = LocalDateTime.now().minusMinutes(1);
		LocalDateTime end = LocalDateTime.now().plusMinutes(1);

		Page<OrderResponse> responses = orderService.listByPeriod(start, end, PageRequest.of(0, 10));

		assertThat(responses.getContent()).isNotEmpty();
	}

	@Test
	@DisplayName("완료 이전 취소는 재고에 영향을 주지 않는다")
	void 완료_이전_취소는_재고에_영향을_주지_않는다() {
		Product product = createProduct("과자", 1500, 4);
		OrderResponse order = orderService.create(createOrderRequest(product.getId(), 2));

		orderService.updateStatus(order.id(), new OrderStatusUpdateRequest(OrderStatus.CANCELED));

		Product updated = productRepository.findById(product.getId()).orElseThrow();
		assertThat(updated.getStockQuantity()).isEqualTo(4);
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
