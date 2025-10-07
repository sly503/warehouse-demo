package com.sample.demo.dto.order;

import com.sample.demo.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private String clientUsername;
    private OrderStatus status;
    private LocalDateTime submittedDate;
    private LocalDate deadlineDate;
    private String declineReason;
    private List<OrderItemDTO> orderItems;
    private DeliveryDTO delivery;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderWarning> warnings;
}