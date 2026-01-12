package com.ksr930.nhnkcp.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ksr930.nhnkcp.domain.product.Category;
import com.ksr930.nhnkcp.domain.product.Product;
import com.ksr930.nhnkcp.dto.product.ProductRequest;
import com.ksr930.nhnkcp.dto.product.ProductResponse;
import com.ksr930.nhnkcp.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ProductServiceTests {
	@Autowired
	private ProductService productService;

	@Autowired
	private ProductRepository productRepository;

	@Test
	@DisplayName("상품을 등록한다")
	void 상품을_등록한다() {
		ProductRequest request = new ProductRequest("라면", 1500, 20, Category.FOOD);

		ProductResponse response = productService.create(request);

		assertThat(response.id()).isNotNull();
		assertThat(response.name()).isEqualTo("라면");
		assertThat(response.price()).isEqualTo(1500);
		assertThat(response.stockQuantity()).isEqualTo(20);
		assertThat(response.category()).isEqualTo(Category.FOOD);
	}

	@Test
	@DisplayName("상품을 수정한다")
	void 상품을_수정한다() {
		Product product = createProduct("주스", 2000, 5, Category.BEVERAGE);

		ProductRequest request = new ProductRequest("오렌지주스", 2500, 7, Category.BEVERAGE);

		ProductResponse response = productService.update(product.getId(), request);

		assertThat(response.name()).isEqualTo("오렌지주스");
		assertThat(response.price()).isEqualTo(2500);
		assertThat(response.stockQuantity()).isEqualTo(7);
	}

	@Test
	@DisplayName("카테고리별 상품 목록을 조회한다")
	void 카테고리별_목록을_조회한다() {
		createProduct("티셔츠", 10000, 3, Category.FASHION);
		createProduct("청바지", 20000, 2, Category.FASHION);
		createProduct("스피커", 50000, 1, Category.ELECTRONICS);

		Page<ProductResponse> responses =
				productService.listByCategory(Category.FASHION, PageRequest.of(0, 10));

		assertThat(responses.getContent()).hasSize(2);
	}

	private Product createProduct(String name, int price, int stock, Category category) {
		Product product = new Product();
		product.setName(name);
		product.setPrice(price);
		product.setStockQuantity(stock);
		product.setCategory(category);
		return productRepository.save(product);
	}
}
