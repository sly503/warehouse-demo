package com.sample.demo.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long id;
    private Long itemId;
    private String itemName;
    private Integer requestedQuantity;
    private BigDecimal priceAtOrder;
    private Double totalVolume;
    private BigDecimal totalPrice;
}