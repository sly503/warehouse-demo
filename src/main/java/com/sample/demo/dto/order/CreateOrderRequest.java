package com.sample.demo.dto.order;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateOrderRequest {

    @NotNull(message = "Deadline date is required")
    @Future(message = "Deadline date must be in the future")
    private LocalDate deadlineDate;

    @NotEmpty(message = "At least one order item is required")
    private List<OrderItemRequest> orderItems;
}