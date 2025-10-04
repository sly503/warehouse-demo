package com.sample.demo.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Item item;

    @Column(nullable = false)
    private Integer requestedQuantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal priceAtOrder; // Store the price at the time of order

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Double getTotalVolume() {
        if (item != null && item.getPackageVolume() != null && requestedQuantity != null) {
            return item.getPackageVolume() * requestedQuantity;
        }
        return 0.0;
    }

    public BigDecimal getTotalPrice() {
        if (priceAtOrder != null && requestedQuantity != null) {
            return priceAtOrder.multiply(BigDecimal.valueOf(requestedQuantity));
        }
        return BigDecimal.ZERO;
    }
}