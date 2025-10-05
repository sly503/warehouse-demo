package com.sample.demo.dto.truck;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateTruckRequest {

    @NotBlank(message = "Chassis number is required")
    @Size(max = 50, message = "Chassis number must not exceed 50 characters")
    private String chassisNumber;

    @NotBlank(message = "License plate is required")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "License plate must contain only uppercase letters, numbers, and hyphens")
    @Size(max = 20, message = "License plate must not exceed 20 characters")
    private String licensePlate;

    @NotNull(message = "Container volume is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Container volume must be greater than 0")
    private Double containerVolume;
}