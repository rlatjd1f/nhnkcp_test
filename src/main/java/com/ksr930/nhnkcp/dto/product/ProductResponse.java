package com.ksr930.nhnkcp.dto.product;

import com.ksr930.nhnkcp.domain.product.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductResponse {
	private Long id;
	private String name;
	private int price;
	private int stockQuantity;
	private Category category;
}
