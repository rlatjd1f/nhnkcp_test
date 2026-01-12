package com.ksr930.nhnkcp.dto.order;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderCreateRequest {
	@Valid
	@NotEmpty
	private List<OrderItemRequest> items = new ArrayList<>();
}
