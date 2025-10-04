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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/manager/trucks")
@RequiredArgsConstructor
@Tag(name = "Truck Management", description = "Truck management endpoints (Warehouse Manager only)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'SYSTEM_ADMIN')")
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

    @GetMapping("/all")
    @Operation(summary = "Get all trucks without pagination", description = "Get all trucks as a list")
    public ResponseEntity<ApiResponse<List<TruckDTO>>> getAllTrucksList() {
        List<TruckDTO> trucks = truckService.getAllTrucks();
        return ResponseEntity.ok(ApiResponse.success("Trucks fetched successfully", trucks));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get truck by ID", description = "Get a specific truck by its ID")
    public ResponseEntity<ApiResponse<TruckDTO>> getTruckById(@PathVariable Long id) {
        TruckDTO truck = truckService.getTruckById(id);
        return ResponseEntity.ok(ApiResponse.success("Truck fetched successfully", truck));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available trucks", description = "Get trucks available for a specific date or generally available")
    public ResponseEntity<ApiResponse<List<TruckDTO>>> getAvailableTrucks(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<TruckDTO> trucks;
        String message;

        if (date != null) {
            trucks = truckService.getAvailableTrucksForDate(date);
            message = "Available trucks for date " + date + " fetched successfully";
        } else {
            trucks = truckService.getAvailableTrucks();
            message = "Available trucks fetched successfully";
        }

        return ResponseEntity.ok(ApiResponse.success(message, trucks));
    }

    @GetMapping("/available-by-volume")
    @Operation(summary = "Get available trucks by volume", description = "Get available trucks with minimum required volume")
    public ResponseEntity<ApiResponse<List<TruckDTO>>> getAvailableTrucksByVolume(
            @RequestParam Double minVolume) {

        List<TruckDTO> trucks = truckService.getAvailableTrucksWithMinVolume(minVolume);
        return ResponseEntity.ok(ApiResponse.success("Available trucks with minimum volume fetched successfully", trucks));
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

    @PatchMapping("/{id}/availability")
    @Operation(summary = "Set truck availability", description = "Mark a truck as available or unavailable")
    public ResponseEntity<ApiResponse<Void>> setTruckAvailability(
            @PathVariable Long id,
            @RequestParam boolean available) {

        truckService.setTruckAvailability(id, available);
        String status = available ? "available" : "unavailable";
        return ResponseEntity.ok(ApiResponse.success("Truck marked as " + status + " successfully", null));
    }
}