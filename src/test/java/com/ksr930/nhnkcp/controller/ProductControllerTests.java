package com.ksr930.nhnkcp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksr930.nhnkcp.domain.product.Category;
import com.ksr930.nhnkcp.dto.product.ProductRequest;
import com.ksr930.nhnkcp.dto.product.ProductResponse;
import com.ksr930.nhnkcp.service.ProductService;
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

@WebMvcTest(ProductController.class)
class ProductControllerTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ProductService productService;

	@Test
	@DisplayName("상품을 등록한다")
	void 상품을_등록한다() throws Exception {
		ProductRequest request = new ProductRequest("콜라", 1000, 5, Category.BEVERAGE);

		when(productService.create(any(ProductRequest.class))).thenReturn(productResponse(1L));

		mockMvc.perform(post("/api/products")
						.contentType("application/json")
						.content(objectMapper.writeValueAsBytes(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.id").value(1L));
	}

	@Test
	@DisplayName("상품을 수정한다")
	void 상품을_수정한다() throws Exception {
		ProductRequest request = new ProductRequest("제로콜라", 1200, 4, Category.BEVERAGE);

		when(productService.update(eq(1L), any(ProductRequest.class))).thenReturn(productResponse(1L));

		mockMvc.perform(put("/api/products/1")
						.contentType("application/json")
						.content(objectMapper.writeValueAsBytes(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(1L));
	}

	@Test
	@DisplayName("상품 단건을 조회한다")
	void 상품을_단건_조회한다() throws Exception {
		when(productService.get(1L)).thenReturn(productResponse(1L));

		mockMvc.perform(get("/api/products/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(1L));
	}

	@Test
	@DisplayName("상품 목록을 조회한다")
	void 상품_목록을_조회한다() throws Exception {
		PageImpl<ProductResponse> page =
				new PageImpl<>(List.of(productResponse(1L)), PageRequest.of(0, 10), 1);
		when(productService.list(any(Pageable.class))).thenReturn(page);

		mockMvc.perform(get("/api/products?page=0&size=10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[0].id").value(1L));
	}

	@Test
	@DisplayName("카테고리별 상품 목록을 조회한다")
	void 카테고리별_상품_목록을_조회한다() throws Exception {
		PageImpl<ProductResponse> page =
				new PageImpl<>(List.of(productResponse(1L)), PageRequest.of(0, 10), 1);
		when(productService.listByCategory(eq(Category.BEVERAGE), any(Pageable.class))).thenReturn(page);

		mockMvc.perform(get("/api/products?category=BEVERAGE&page=0&size=10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[0].id").value(1L));

		verify(productService).listByCategory(eq(Category.BEVERAGE), any(Pageable.class));
	}

	@Test
	@DisplayName("상품 등록 요청이 잘못되면 실패한다")
	void 상품_등록_요청이_잘못되면_실패한다() throws Exception {
		mockMvc.perform(post("/api/products")
						.contentType("application/json")
						.content("{}"))
				.andExpect(status().isBadRequest());
	}

	private ProductResponse productResponse(Long id) {
		return new ProductResponse(id, "콜라", 1000, 5, Category.BEVERAGE);
	}
}
