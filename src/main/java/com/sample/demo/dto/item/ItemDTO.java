package com.sample.demo.dto.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDTO {
    private Long id;
    private String itemName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private Double packageVolume;
    private String description;
    private String sku;
}