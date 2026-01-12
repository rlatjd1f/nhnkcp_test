package com.ksr930.nhnkcp.dto.product;

import com.ksr930.nhnkcp.domain.product.Category;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductRequest {
	@NotBlank
	private String name;

	@Min(0)
	private int price;

	@Min(0)
	private int stockQuantity;

	@NotNull
	private Category category;
}
