package com.sample.demo.dto.truck;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruckDTO {
    private Long id;
    private String chassisNumber;
    private String licensePlate;
    private Double containerVolume;
    private boolean available;
    private String driverName;
    private String notes;
}