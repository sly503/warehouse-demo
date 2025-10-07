package com.sample.demo.controller;

import com.sample.demo.dto.common.ApiResponse;
import com.sample.demo.dto.config.UpdateDeliveryPeriodRequest;
import com.sample.demo.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
@Tag(name = "System Configuration", description = "System configuration endpoints (Admin only)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class SystemConfigController {

    private final SystemConfigService configService;

    @GetMapping("/delivery-period")
    @Operation(summary = "Get delivery period", description = "Get current delivery lookup period in days")
    public ResponseEntity<ApiResponse<Integer>> getDeliveryPeriod() {
        int days = configService.getDeliveryPeriod();
        return ResponseEntity.ok(ApiResponse.success("Delivery period retrieved", days));
    }

    @PutMapping("/delivery-period")
    @Operation(summary = "Update delivery period", description = "Update delivery lookup period (1-30 days)")
    public ResponseEntity<ApiResponse<Integer>> updateDeliveryPeriod(@Valid @RequestBody UpdateDeliveryPeriodRequest request) {
        configService.updateDeliveryPeriod(request.getDays());
        return ResponseEntity.ok(ApiResponse.success("Delivery period updated", request.getDays()));
    }
}

