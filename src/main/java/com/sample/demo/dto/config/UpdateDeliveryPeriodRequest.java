package com.sample.demo.dto.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeliveryPeriodRequest {

    @NotNull(message = "Days is required")
    @Min(value = 1, message = "Delivery period must be at least 1 day")
    @Max(value = 30, message = "Delivery period cannot exceed 30 days")
    private Integer days;
}
