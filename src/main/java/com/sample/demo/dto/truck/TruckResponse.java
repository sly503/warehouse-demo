package com.sample.demo.dto.truck;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruckResponse {
    private Long id;
    private String chassisNumber;
    private String licensePlate;
    private Double containerVolume;
}
