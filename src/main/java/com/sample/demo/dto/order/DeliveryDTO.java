package com.sample.demo.dto.order;

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
public class DeliveryDTO {
    private Long id;
    private LocalDate scheduledDate;
    private List<Long> truckIds;
    private Double totalVolume;
    private boolean completed;
    private LocalDateTime completedAt;
    private String notes;
}