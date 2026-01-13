package com.ksr930.nhnkcp.service;

import com.ksr930.nhnkcp.domain.order.OrderItem;
import com.ksr930.nhnkcp.domain.product.Product;
import com.ksr930.nhnkcp.exception.ApiException;
import com.ksr930.nhnkcp.exception.ErrorCode;
import com.ksr930.nhnkcp.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StockService {
	private final ProductRepository productRepository;

	public StockService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public Product getProduct(Long productId) {
		return productRepository.findByIdForUpdate(productId)
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
	}

	public void decrease(List<OrderItem> items) {
		for (OrderItem item : items) {
			Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
					.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
			if (product.getStockQuantity() < item.getQuantity()) {
				throw new ApiException(ErrorCode.OUT_OF_STOCK);
			}
			product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
		}
	}

	public void restore(List<OrderItem> items) {
		for (OrderItem item : items) {
			Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
					.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
			product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
		}
	}
}
