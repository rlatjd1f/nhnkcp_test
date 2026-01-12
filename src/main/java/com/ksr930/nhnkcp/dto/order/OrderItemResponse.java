package com.ksr930.nhnkcp.dto.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderItemResponse {
	private Long productId;
	private String productName;
	private int price;
	private int quantity;
}
