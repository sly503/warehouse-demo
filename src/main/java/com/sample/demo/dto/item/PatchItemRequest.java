package com.sample.demo.dto.item;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PatchItemRequest {

    @Size(max = 100, message = "Item name must not exceed 100 characters")
    private String itemName;

    @Min(value = 0, message = "Quantity must be at least 0")
    private Integer quantity;

    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "Package volume must be greater than 0")
    private Double packageVolume;

    private String description;

    private String sku;
}
