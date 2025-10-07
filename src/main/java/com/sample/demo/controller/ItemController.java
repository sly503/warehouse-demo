package com.sample.demo.controller;

import com.sample.demo.dto.common.ApiResponse;
import com.sample.demo.dto.item.ItemRequest;
import com.sample.demo.dto.item.ItemResponse;
import com.sample.demo.dto.item.PatchItemRequest;
import com.sample.demo.service.ItemService;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Item Management", description = "Item management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items")
    @Operation(summary = "Get all items", description = "Get all items with pagination (All authenticated users can view)")
    public ResponseEntity<ApiResponse<Page<ItemResponse>>> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ItemResponse> items = itemService.getAllItems(pageable);
        return ResponseEntity.ok(ApiResponse.success("Items fetched successfully", items));
    }

    @GetMapping("/items/{id}")
    @Operation(summary = "Get item by ID", description = "Get a specific item by its ID (All authenticated users can view)")
    public ResponseEntity<ApiResponse<ItemResponse>> getItemById(@PathVariable Long id) {
        ItemResponse item = itemService.getItemById(id);
        return ResponseEntity.ok(ApiResponse.success("Item fetched successfully", item));
    }

    @PostMapping("/manager/items")
    @Operation(summary = "Create new item", description = "Create a new item (Warehouse Manager only)")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<ItemResponse>> createItem(@Valid @RequestBody ItemRequest request) {
        ItemResponse item = itemService.createItem(request);
        return new ResponseEntity<>(ApiResponse.success("Item created successfully", item), HttpStatus.CREATED);
    }

    @PutMapping("/manager/items/{id}")
    @Operation(summary = "Update item (full)", description = "Replace entire item (all fields required) (Warehouse Manager only)")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<ItemResponse>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequest request) {

        ItemResponse item = itemService.updateItem(id, request);
        return ResponseEntity.ok(ApiResponse.success("Item updated successfully", item));
    }

    @PatchMapping("/manager/items/{id}")
    @Operation(summary = "Update item (partial)", description = "Update specific fields only (Warehouse Manager only)")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<ItemResponse>> patchItem(
            @PathVariable Long id,
            @Valid @RequestBody PatchItemRequest request) {

        ItemResponse item = itemService.patchItem(id, request);
        return ResponseEntity.ok(ApiResponse.success("Item updated successfully", item));
    }

    @DeleteMapping("/manager/items/{id}")
    @Operation(summary = "Delete item", description = "Delete an item (Warehouse Manager only)")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.success("Item deleted successfully", null));
    }

}