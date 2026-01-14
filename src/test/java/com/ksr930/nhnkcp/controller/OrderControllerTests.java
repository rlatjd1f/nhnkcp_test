package com.ksr930.nhnkcp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksr930.nhnkcp.domain.order.OrderStatus;
import com.ksr930.nhnkcp.dto.order.OrderCreateRequest;
import com.ksr930.nhnkcp.dto.order.OrderItemRequest;
import com.ksr930.nhnkcp.dto.order.OrderResponse;
import com.ksr930.nhnkcp.dto.order.OrderStatusUpdateRequest;
import com.ksr930.nhnkcp.service.OrderService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
class OrderControllerTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private OrderService orderService;

	@Test
	@DisplayName("주문을 생성한다")
	void 주문을_생성한다() throws Exception {
		// 테스트 대상: OrderController#create, 의도: 요청 본문 바인딩 및 201 응답 검증
		OrderCreateRequest request = new OrderCreateRequest(List.of(new OrderItemRequest(1L, 2)));

		when(orderService.create(any(OrderCreateRequest.class))).thenReturn(orderResponse(1L));

		mockMvc.perform(post("/api/orders")
						.contentType("application/json")
						.content(objectMapper.writeValueAsBytes(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.id").value(1L));
	}

	@Test
	@DisplayName("주문 상태를 변경한다")
	void 주문_상태를_변경한다() throws Exception {
		// 테스트 대상: OrderController#updateStatus, 의도: PATCH 요청 처리 및 응답 검증
		OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderStatus.RECEIVED);

		when(orderService.updateStatus(eq(1L), any(OrderStatusUpdateRequest.class)))
				.thenReturn(orderResponse(1L));

		mockMvc.perform(patch("/api/orders/1/status")
						.contentType("application/json")
						.content(objectMapper.writeValueAsBytes(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(1L));
	}

	@Test
	@DisplayName("주문 단건을 조회한다")
	void 주문을_단건_조회한다() throws Exception {
		// 테스트 대상: OrderController#get, 의도: 단건 조회 응답 검증
		when(orderService.get(1L)).thenReturn(orderResponse(1L));

		mockMvc.perform(get("/api/orders/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(1L));
	}

	@Test
	@DisplayName("주문 목록을 조회한다")
	void 주문_목록을_조회한다() throws Exception {
		// 테스트 대상: OrderController#list, 의도: 페이징 목록 조회 응답 검증
		PageImpl<OrderResponse> page =
				new PageImpl<>(List.of(orderResponse(1L)), PageRequest.of(0, 10), 1);
		when(orderService.list(any(Pageable.class))).thenReturn(page);

		mockMvc.perform(get("/api/orders?page=0&size=10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[0].id").value(1L));
	}

	@Test
	@DisplayName("상태별 주문 목록을 조회한다")
	void 상태별_주문_목록을_조회한다() throws Exception {
		// 테스트 대상: OrderController#list (status 필터), 의도: status 파라미터 분기 검증
		PageImpl<OrderResponse> page =
				new PageImpl<>(List.of(orderResponse(1L)), PageRequest.of(0, 10), 1);
		when(orderService.listByStatus(eq(OrderStatus.RECEIVED), any(Pageable.class))).thenReturn(page);

		mockMvc.perform(get("/api/orders?status=RECEIVED&page=0&size=10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[0].id").value(1L));

		verify(orderService).listByStatus(eq(OrderStatus.RECEIVED), any(Pageable.class));
	}

	@Test
	@DisplayName("기간별 주문 목록을 조회한다")
	void 기간별_주문_목록을_조회한다() throws Exception {
		// 테스트 대상: OrderController#list (기간 필터), 의도: start/end 파라미터 분기 검증
		PageImpl<OrderResponse> page =
				new PageImpl<>(List.of(orderResponse(1L)), PageRequest.of(0, 10), 1);
		when(orderService.listByPeriod(any(), any(), any(Pageable.class))).thenReturn(page);

		mockMvc.perform(get("/api/orders?start=2025-01-01T00:00:00&end=2025-12-31T23:59:59&page=0&size=10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[0].id").value(1L));
	}

	@Test
	@DisplayName("주문 생성 요청이 잘못되면 실패한다")
	void 주문_생성_요청이_잘못되면_실패한다() throws Exception {
		// 테스트 대상: OrderController#create, 의도: 유효성 실패 시 400 응답 검증
		mockMvc.perform(post("/api/orders")
						.contentType("application/json")
						.content("{\"items\":[]}"))
				.andExpect(status().isBadRequest());
	}

	private OrderResponse orderResponse(Long id) {
		return new OrderResponse(id, OrderStatus.PENDING, null, null, List.of());
	}
}
