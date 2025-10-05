package com.sample.demo.controller;

import com.sample.demo.dto.common.ApiResponse;
import com.sample.demo.dto.order.CreateOrderRequest;
import com.sample.demo.dto.order.DeclineOrderRequest;
import com.sample.demo.dto.order.OrderDTO;
import com.sample.demo.dto.order.ScheduleDeliveryRequest;
import com.sample.demo.dto.order.UpdateOrderItemsRequest;
import com.sample.demo.model.entity.User;
import com.sample.demo.model.enums.WorkflowStatus;
import com.sample.demo.service.OrderService;
import com.sample.demo.service.OrderWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
@Tag(name = "Order Workflow (Temporal)", description = "Temporal workflow-based order management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class OrderWorkflowController {

    private final OrderWorkflowService orderWorkflowService;
    private final OrderService orderService;

    @PostMapping("/client/orders")
    @Operation(summary = "Create order and start workflow", description = "Create a new order and start workflow (CLIENT only)")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createOrderWithWorkflow(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateOrderRequest request) {

        log.info("Creating order with Temporal workflow for client: {}", currentUser.getUsername());

        OrderDTO order = orderService.createOrder(currentUser.getUsername(), request);
        String workflowId = orderWorkflowService.startOrderWorkflow(order.getId(), currentUser.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("order", order);
        response.put("workflowId", workflowId);

        return new ResponseEntity<>(ApiResponse.success("Order created and workflow started successfully", response), HttpStatus.CREATED);
    }

    @PutMapping("/client/orders/{orderId}/items")
    @Operation(summary = "Update order items via workflow", description = "Update order items via workflow Update method (CLIENT only)")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<String>> updateOrderItemsWorkflow(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderItemsRequest request) {

        log.info("Updating order items via workflow for order: {}", orderId);
        String result = orderWorkflowService.updateOrderItems(orderId, request);
        return ResponseEntity.ok(ApiResponse.success(result, result));
    }

    @PostMapping("/client/orders/{orderId}/submit")
    @Operation(summary = "Submit order via workflow", description = "Submit order for approval via workflow (CLIENT only)")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<String>> submitOrderWorkflow(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId) {

        log.info("Submitting order via workflow: {}", orderId);
        orderWorkflowService.submitOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order submitted via workflow successfully", "Workflow signal sent"));
    }

    @PostMapping("/client/orders/{orderId}/cancel")
    @Operation(summary = "Cancel order via workflow", description = "Cancel order via workflow (CLIENT only)")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<String>> cancelOrderWorkflow(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId) {

        log.info("Cancelling order via workflow: {}", orderId);
        orderWorkflowService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order cancel signal sent via workflow", "Workflow signal sent"));
    }

    @PostMapping("/manager/orders/{orderId}/approve")
    @Operation(summary = "Approve order via workflow", description = "Approve order via workflow (WAREHOUSE_MANAGER only)")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<String>> approveOrderWorkflow(@PathVariable Long orderId) {

        log.info("Approving order via workflow: {}", orderId);
        orderWorkflowService.approveOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order approved via workflow successfully", "Workflow signal sent"));
    }

    @PostMapping("/manager/orders/{orderId}/decline")
    @Operation(summary = "Decline order via workflow", description = "Decline order via workflow (WAREHOUSE_MANAGER only)")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<String>> declineOrderWorkflow(
            @PathVariable Long orderId,
            @Valid @RequestBody DeclineOrderRequest request) {

        log.info("Declining order via workflow: {}", orderId);
        orderWorkflowService.declineOrder(orderId, request.getDeclineReason());
        return ResponseEntity.ok(ApiResponse.success("Order declined via workflow successfully", "Workflow signal sent"));
    }

    @PostMapping("/manager/orders/{orderId}/schedule-delivery")
    @Operation(summary = "Schedule delivery via workflow", description = "Schedule delivery via  workflow (WAREHOUSE_MANAGER only)")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<String>> scheduleDeliveryWorkflow(
            @PathVariable Long orderId,
            @Valid @RequestBody ScheduleDeliveryRequest request) {

        log.info("Scheduling delivery via workflow for order: {}", orderId);
        orderWorkflowService.scheduleDelivery(orderId, request.getScheduledDate(), request.getTruckIds(), request.getNotes());
        return ResponseEntity.ok(ApiResponse.success("Delivery scheduled via workflow successfully", "Workflow signal sent"));
    }

    @GetMapping("/orders/{orderId}/status")
    @Operation(summary = "[Temporal] Get workflow status", description = "Get current workflow status for an order")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWorkflowStatus(@PathVariable Long orderId) {

        log.info("Fetching workflow status for order: {}", orderId);
        WorkflowStatus status = orderWorkflowService.getWorkflowStatus(orderId);
        String declineReason = orderWorkflowService.getDeclineReason(orderId);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("workflowStatus", status);
        response.put("declineReason", declineReason);

        return ResponseEntity.ok(ApiResponse.success("Workflow status fetched successfully", response));
    }
}
