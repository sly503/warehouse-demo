package com.sample.demo.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeclineOrderRequest {

    @NotBlank(message = "Decline reason is required")
    private String declineReason;
}
