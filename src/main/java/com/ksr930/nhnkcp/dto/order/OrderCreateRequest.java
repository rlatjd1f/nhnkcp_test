package com.ksr930.nhnkcp.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderCreateRequest(
		@Valid @NotEmpty List<OrderItemRequest> items
) {
}
