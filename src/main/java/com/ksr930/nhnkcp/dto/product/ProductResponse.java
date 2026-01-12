package com.ksr930.nhnkcp.dto.product;

import com.ksr930.nhnkcp.domain.product.Category;

public record ProductResponse(
		Long id,
		String name,
		int price,
		int stockQuantity,
		Category category
) {
}
