package com.sample.demo.controller;

import com.sample.demo.dto.common.ApiResponse;
import com.sample.demo.dto.order.*;
import com.sample.demo.model.entity.User;
import com.sample.demo.model.enums.OrderStatus;
import com.sample.demo.service.OrderService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Order management endpoints for clients and warehouse managers")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    // ==================== CLIENT ENDPOINTS ====================

    @PostMapping("/client/orders")
    @Operation(summary = "Create order", description = "Create a new order (CLIENT only)")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateOrderRequest request) {

        log.info("Creating order for client: {}", currentUser.getUsername());
        OrderDTO order = orderService.createOrder(currentUser.getUsername(), request);
        return new ResponseEntity<>(ApiResponse.success("Order created successfully", order), HttpStatus.CREATED);
    }

    @GetMapping("/client/orders")
    @Operation(summary = "Get client orders", description = "Get all orders for the authenticated client with optional status filter (CLIENT only)")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getClientOrders(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<OrderDTO> orders = orderService.getClientOrders(currentUser.getUsername(), status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Orders fetched successfully", orders));
    }

    @GetMapping("/client/orders/{orderId}")
    @Operation(summary = "Get client order by ID", description = "Get a specific order by ID for the authenticated client (CLIENT only)")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<OrderDTO>> getClientOrderById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId) {

        OrderDTO order = orderService.getClientOrderById(currentUser.getUsername(), orderId);
        return ResponseEntity.ok(ApiResponse.success("Order fetched successfully", order));
    }

    @PutMapping("/client/orders/{orderId}/items")
    @Operation(summary = "Update order items", description = "Update order items (add/remove/modify) when status is CREATED or DECLINED (CLIENT only)")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderItems(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderItemsRequest request) {

        log.info("Updating order items for order: {}", orderId);
        OrderDTO order = orderService.updateOrderItems(currentUser.getUsername(), orderId, request);
        return ResponseEntity.ok(ApiResponse.success("Order items updated successfully", order));
    }

    @PostMapping("/client/orders/{orderId}/submit")
    @Operation(summary = "Submit order", description = "Submit order for approval when status is CREATED or DECLINED (CLIENT only)")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<OrderDTO>> submitOrder(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId) {

        log.info("Submitting order: {}", orderId);
        OrderDTO order = orderService.submitOrder(currentUser.getUsername(), orderId);
        return ResponseEntity.ok(ApiResponse.success("Order submitted successfully", order));
    }

    @PostMapping("/client/orders/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel order when status is not FULFILLED, UNDER_DELIVERY or CANCELED (CLIENT only)")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId) {

        log.info("Cancelling order: {}", orderId);
        orderService.cancelOrder(currentUser.getUsername(), orderId);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", null));
    }

    // ==================== WAREHOUSE MANAGER ENDPOINTS ====================

    @GetMapping("/manager/orders")
    @Operation(summary = "Get all orders", description = "Get all orders with optional status filter, sorted by submission date DESC (WAREHOUSE_MANAGER only)")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<Page<OrderSummaryDTO>>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Always sort by submittedDate DESC for manager view
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedDate"));

        Page<OrderSummaryDTO> orders = orderService.getAllOrders(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Orders fetched successfully", orders));
    }

    @GetMapping("/manager/orders/{orderId}")
    @Operation(summary = "Get order details", description = "Get detailed information about a specific order (WAREHOUSE_MANAGER only)")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Long orderId) {
        OrderDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order details fetched successfully", order));
    }

    @PostMapping("/manager/orders/{orderId}/approve")
    @Operation(summary = "Approve order", description = "Approve an order that is awaiting approval (WAREHOUSE_MANAGER only)")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<OrderDTO>> approveOrder(@PathVariable Long orderId) {
        log.info("Approving order: {}", orderId);
        OrderDTO order = orderService.approveOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order approved successfully", order));
    }

    @PostMapping("/manager/orders/{orderId}/decline")
    @Operation(summary = "Decline order", description = "Decline an order with a reason when awaiting approval (WAREHOUSE_MANAGER only)")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<OrderDTO>> declineOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody DeclineOrderRequest request) {

        log.info("Declining order: {}", orderId);
        OrderDTO order = orderService.declineOrder(orderId, request);
        return ResponseEntity.ok(ApiResponse.success("Order declined successfully", order));
    }

    @PostMapping("/manager/orders/{orderId}/schedule-delivery")
    @Operation(summary = "Schedule delivery", description = "Schedule delivery for an approved order with selected trucks (WAREHOUSE_MANAGER only)")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<OrderDTO>> scheduleDelivery(
            @PathVariable Long orderId,
            @Valid @RequestBody ScheduleDeliveryRequest request) {

        log.info("Scheduling delivery for order: {}", orderId);
        OrderDTO order = orderService.scheduleDelivery(orderId, request);
        return ResponseEntity.ok(ApiResponse.success("Delivery scheduled successfully", order));
    }

    @GetMapping("/manager/orders/{orderId}/available-delivery-dates")
    @Operation(summary = "Get available delivery dates", description = "Get available delivery dates for an order within a specified period (3-30 days) (WAREHOUSE_MANAGER only)")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<List<LocalDate>>> getAvailableDeliveryDates(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "7") int days) {

        log.info("Fetching available delivery dates for order: {} for {} days", orderId, days);
        List<LocalDate> availableDates = orderService.getAvailableDeliveryDates(orderId, days);
        return ResponseEntity.ok(ApiResponse.success("Available delivery dates fetched successfully", availableDates));
    }
}
