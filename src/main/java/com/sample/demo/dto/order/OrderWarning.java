package com.sample.demo.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderWarning {

    private String type;
    private String message;
    private String severity; // INFO, WARNING, ERROR
}
