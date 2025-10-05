package com.sample.demo.dto.order;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateOrderItemsRequest {

    @NotEmpty(message = "At least one order item is required")
    private List<OrderItemRequest> orderItems;
}
