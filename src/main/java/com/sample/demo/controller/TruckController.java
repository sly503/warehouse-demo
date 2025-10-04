package com.sample.demo.controller;

import com.sample.demo.dto.common.ApiResponse;
import com.sample.demo.dto.truck.CreateTruckRequest;
import com.sample.demo.dto.truck.TruckDTO;
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
    public ResponseEntity<ApiResponse<Page<TruckDTO>>> getAllTrucks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<TruckDTO> trucks = truckService.getAllTrucks(pageable);
        return ResponseEntity.ok(ApiResponse.success("Trucks fetched successfully", trucks));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get truck by ID", description = "Get a specific truck by its ID")
    public ResponseEntity<ApiResponse<TruckDTO>> getTruckById(@PathVariable Long id) {
        TruckDTO truck = truckService.getTruckById(id);
        return ResponseEntity.ok(ApiResponse.success("Truck fetched successfully", truck));
    }

    @PostMapping
    @Operation(summary = "Create new truck", description = "Register a new truck in the system")
    public ResponseEntity<ApiResponse<TruckDTO>> createTruck(@Valid @RequestBody CreateTruckRequest request) {
        TruckDTO truck = truckService.createTruck(request);
        return new ResponseEntity<>(ApiResponse.success("Truck created successfully", truck), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update truck", description = "Update an existing truck")
    public ResponseEntity<ApiResponse<TruckDTO>> updateTruck(
            @PathVariable Long id,
            @Valid @RequestBody CreateTruckRequest request) {

        TruckDTO truck = truckService.updateTruck(id, request);
        return ResponseEntity.ok(ApiResponse.success("Truck updated successfully", truck));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete truck", description = "Remove a truck from the system")
    public ResponseEntity<ApiResponse<Void>> deleteTruck(@PathVariable Long id) {
        truckService.deleteTruck(id);
        return ResponseEntity.ok(ApiResponse.success("Truck deleted successfully", null));
    }

}