package com.sample.demo.controller;

import com.sample.demo.dto.common.ApiResponse;
import com.sample.demo.dto.truck.TruckRequest;
import com.sample.demo.dto.truck.PatchTruckRequest;
import com.sample.demo.dto.truck.TruckResponse;
import com.sample.demo.service.TruckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/manager/trucks")
@RequiredArgsConstructor
@Tag(name = "Truck Management", description = "Truck management endpoints (Warehouse Manager only)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
public class TruckController {

    private final TruckService truckService;

    @GetMapping
    @Operation(summary = "Get all trucks", description = "Get all trucks with pagination")
    public ResponseEntity<ApiResponse<Page<TruckResponse>>> getAllTrucks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<TruckResponse> trucks = truckService.getAllTrucks(pageable);
        return ResponseEntity.ok(ApiResponse.success("Trucks fetched successfully", trucks));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get truck by ID", description = "Get a specific truck by its ID")
    public ResponseEntity<ApiResponse<TruckResponse>> getTruckById(@PathVariable Long id) {
        TruckResponse truck = truckService.getTruckById(id);
        return ResponseEntity.ok(ApiResponse.success("Truck fetched successfully", truck));
    }

    @PostMapping
    @Operation(summary = "Create new truck", description = "Register a new truck in the system")
    public ResponseEntity<ApiResponse<TruckResponse>> createTruck(@Valid @RequestBody TruckRequest request) {
        TruckResponse truck = truckService.createTruck(request);
        return new ResponseEntity<>(ApiResponse.success("Truck created successfully", truck), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update truck", description = "Update an existing truck")
    public ResponseEntity<ApiResponse<TruckResponse>> updateTruck(
            @PathVariable Long id,
            @Valid @RequestBody TruckRequest request) {

        TruckResponse truck = truckService.updateTruck(id, request);
        return ResponseEntity.ok(ApiResponse.success("Truck updated successfully", truck));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update truck (partial)", description = "Update specific fields only")
    public ResponseEntity<ApiResponse<TruckResponse>> patchTruck(
            @PathVariable Long id,
            @Valid @RequestBody PatchTruckRequest request) {

        TruckResponse truck = truckService.patchTruck(id, request);
        return ResponseEntity.ok(ApiResponse.success("Truck updated successfully", truck));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete truck", description = "Remove a truck from the system")
    public ResponseEntity<ApiResponse<Void>> deleteTruck(@PathVariable Long id) {
        truckService.deleteTruck(id);
        return ResponseEntity.ok(ApiResponse.success("Truck deleted successfully", null));
    }

}