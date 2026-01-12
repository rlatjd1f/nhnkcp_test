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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class OrderServiceTests {
	@Autowired
	private OrderService orderService;

	@Autowired
	private ProductRepository productRepository;

	@Test
	void 주문_완료_시_재고가_차감된다() {
		Product product = createProduct("콜라", 1000, 5);
		OrderResponse order = orderService.create(createOrderRequest(product.getId(), 2));

		OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
		request.setStatus(OrderStatus.RECEIVED);
		orderService.updateStatus(order.getId(), request);

		request.setStatus(OrderStatus.COMPLETED);
		orderService.updateStatus(order.getId(), request);

		Product updated = productRepository.findById(product.getId()).orElseThrow();
		assertThat(updated.getStockQuantity()).isEqualTo(3);
	}

	@Test
	void 완료된_주문을_취소하면_재고가_복구된다() {
		Product product = createProduct("커피", 2000, 3);
		OrderResponse order = orderService.create(createOrderRequest(product.getId(), 2));

		OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
		request.setStatus(OrderStatus.RECEIVED);
		orderService.updateStatus(order.getId(), request);
		request.setStatus(OrderStatus.COMPLETED);
		orderService.updateStatus(order.getId(), request);
		request.setStatus(OrderStatus.CANCELED);
		orderService.updateStatus(order.getId(), request);

		Product updated = productRepository.findById(product.getId()).orElseThrow();
		assertThat(updated.getStockQuantity()).isEqualTo(3);
	}

	@Test
	void 재고가_부족하면_주문_생성이_실패한다() {
		Product product = createProduct("샌드위치", 5000, 1);

		assertThatThrownBy(() -> orderService.create(createOrderRequest(product.getId(), 2)))
				.isInstanceOf(ApiException.class);
	}

	@Test
	void 잘못된_상태_변경은_실패한다() {
		Product product = createProduct("케이크", 12000, 2);
		OrderResponse order = orderService.create(createOrderRequest(product.getId(), 1));

		OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
		request.setStatus(OrderStatus.COMPLETED);
		assertThatThrownBy(() -> orderService.updateStatus(order.getId(), request))
				.isInstanceOf(ApiException.class);
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
		OrderItemRequest item = new OrderItemRequest();
		item.setProductId(productId);
		item.setQuantity(quantity);
		OrderCreateRequest request = new OrderCreateRequest();
		request.setItems(List.of(item));
		return request;
	}
}
