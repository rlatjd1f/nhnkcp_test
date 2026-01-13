package com.ksr930.nhnkcp.service;

import com.ksr930.nhnkcp.domain.product.Category;
import com.ksr930.nhnkcp.domain.product.Product;
import com.ksr930.nhnkcp.dto.product.ProductRequest;
import com.ksr930.nhnkcp.dto.product.ProductResponse;
import com.ksr930.nhnkcp.exception.ApiException;
import com.ksr930.nhnkcp.exception.ErrorCode;
import com.ksr930.nhnkcp.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
	private final ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@Transactional
	public ProductResponse create(ProductRequest request) {
		Product product = new Product();
		applyRequest(product, request);
		Product saved = productRepository.save(product);
		return toResponse(saved);
	}

	@Transactional
	public ProductResponse update(Long id, ProductRequest request) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
		applyRequest(product, request);
		return toResponse(product);
	}

	@Transactional(readOnly = true)
	public ProductResponse get(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
		return toResponse(product);
	}

	@Transactional(readOnly = true)
	public Page<ProductResponse> list(Pageable pageable) {
		return productRepository.findAll(pageable).map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public Page<ProductResponse> listByCategory(Category category, Pageable pageable) {
		return productRepository.findAllByCategory(category, pageable).map(this::toResponse);
	}

	private void applyRequest(Product product, ProductRequest request) {
		product.setName(request.name());
		product.setPrice(request.price());
		product.setStockQuantity(request.stockQuantity());
		product.setCategory(request.category());
	}

	private ProductResponse toResponse(Product product) {
		return new ProductResponse(
				product.getId(),
				product.getName(),
				product.getPrice(),
				product.getStockQuantity(),
				product.getCategory()
		);
	}
}
