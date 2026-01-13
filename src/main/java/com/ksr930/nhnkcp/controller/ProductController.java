package com.ksr930.nhnkcp.controller;

import com.ksr930.nhnkcp.domain.product.Category;
import com.ksr930.nhnkcp.dto.ApiResponse;
import com.ksr930.nhnkcp.dto.product.ProductRequest;
import com.ksr930.nhnkcp.dto.product.ProductResponse;
import com.ksr930.nhnkcp.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {
	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody ProductRequest request) {
		return ResponseEntity.status(201)
				.body(ApiResponse.success(201, productService.create(request)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<ProductResponse>> update(
			@PathVariable Long id,
			@Valid @RequestBody ProductRequest request
	) {
		return ResponseEntity.ok(ApiResponse.success(productService.update(id, request)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ProductResponse>> get(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.success(productService.get(id)));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<Page<ProductResponse>>> list(
			@RequestParam(required = false) Category category,
			Pageable pageable
	) {
		if (category == null) {
			return ResponseEntity.ok(ApiResponse.success(productService.list(pageable)));
		}
		return ResponseEntity.ok(ApiResponse.success(productService.listByCategory(category, pageable)));
	}
}
