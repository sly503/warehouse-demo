package com.sample.demo.controller;

import com.sample.demo.dto.common.ApiResponse;
import com.sample.demo.dto.item.CreateItemRequest;
import com.sample.demo.dto.item.ItemDTO;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Item Management", description = "Item management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items")
    @Operation(summary = "Get all items", description = "Get all items with pagination (authenticated users)")
    public ResponseEntity<ApiResponse<Page<ItemDTO>>> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ItemDTO> items = itemService.getAllItems(pageable);
        return ResponseEntity.ok(ApiResponse.success("Items fetched successfully", items));
    }

    @GetMapping("/items/{id}")
    @Operation(summary = "Get item by ID", description = "Get a specific item by its ID")
    public ResponseEntity<ApiResponse<ItemDTO>> getItemById(@PathVariable Long id) {
        ItemDTO item = itemService.getItemById(id);
        return ResponseEntity.ok(ApiResponse.success("Item fetched successfully", item));
    }

    @GetMapping("/items/search")
    @Operation(summary = "Search items", description = "Search items by name")
    public ResponseEntity<ApiResponse<List<ItemDTO>>> searchItems(@RequestParam String name) {
        List<ItemDTO> items = itemService.searchItemsByName(name);
        return ResponseEntity.ok(ApiResponse.success("Items searched successfully", items));
    }

    @GetMapping("/items/available")
    @Operation(summary = "Get available items", description = "Get items with quantity greater than 0")
    public ResponseEntity<ApiResponse<List<ItemDTO>>> getAvailableItems() {
        List<ItemDTO> items = itemService.getAvailableItems();
        return ResponseEntity.ok(ApiResponse.success("Available items fetched successfully", items));
    }

    @GetMapping("/items/low-stock")
    @Operation(summary = "Get low stock items", description = "Get items below threshold quantity")
    public ResponseEntity<ApiResponse<List<ItemDTO>>> getLowStockItems(
            @RequestParam(defaultValue = "10") Integer threshold) {
        List<ItemDTO> items = itemService.getLowStockItems(threshold);
        return ResponseEntity.ok(ApiResponse.success("Low stock items fetched successfully", items));
    }

    @GetMapping("/items/{id}/availability")
    @Operation(summary = "Check item availability", description = "Check if requested quantity is available")
    public ResponseEntity<ApiResponse<Boolean>> checkAvailability(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        boolean available = itemService.checkAvailability(id, quantity);
        String message = available ? "Requested quantity is available" : "Requested quantity is not available";
        return ResponseEntity.ok(ApiResponse.success(message, available));
    }

    @PostMapping("/manager/items")
    @Operation(summary = "Create new item", description = "Create a new item (Warehouse Manager only)")
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<ItemDTO>> createItem(@Valid @RequestBody CreateItemRequest request) {
        ItemDTO item = itemService.createItem(request);
        return new ResponseEntity<>(ApiResponse.success("Item created successfully", item), HttpStatus.CREATED);
    }

    @PutMapping("/manager/items/{id}")
    @Operation(summary = "Update item", description = "Update an existing item (Warehouse Manager only)")
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<ItemDTO>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody CreateItemRequest request) {

        ItemDTO item = itemService.updateItem(id, request);
        return ResponseEntity.ok(ApiResponse.success("Item updated successfully", item));
    }

    @DeleteMapping("/manager/items/{id}")
    @Operation(summary = "Delete item", description = "Delete an item (Warehouse Manager only)")
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.success("Item deleted successfully", null));
    }

    @PatchMapping("/manager/items/{id}/quantity")
    @Operation(summary = "Adjust item quantity", description = "Adjust item quantity (Warehouse Manager only)")
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adjustItemQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantityChange) {

        itemService.updateItemQuantity(id, quantityChange);
        return ResponseEntity.ok(ApiResponse.success("Item quantity adjusted successfully", null));
    }
}