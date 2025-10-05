package com.sample.demo.workflows;

import com.sample.demo.activities.OrderActivities;
import com.sample.demo.model.enums.OrderStatus;
import com.sample.demo.model.enums.WorkflowStatus;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@WorkflowImpl(workers = OrderWorkflow.TASK_QUEUE)
public class OrderWorkflowImpl implements OrderWorkflow {

    private WorkflowStatus status = WorkflowStatus.CREATED;
    private String declineReason;
    private Long orderId;
    private LocalDate scheduledDeliveryDate;

    private final OrderActivities activities;

    public OrderWorkflowImpl() {
        ActivityOptions activityOptions = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofMinutes(5))
                .setRetryOptions(RetryOptions.newBuilder()
                        .setMaximumAttempts(3)
                        .build())
                .build();

        this.activities = Workflow.newActivityStub(OrderActivities.class, activityOptions);
    }

    @Override
    public void processOrder(Long orderId, String clientUsername) {
        this.orderId = orderId;
        log.info("Starting order workflow for order: {}", orderId);

        while (status != WorkflowStatus.FULFILLED && status != WorkflowStatus.CANCELED) {

            if (status == WorkflowStatus.CREATED) {
                Workflow.await(() -> status != WorkflowStatus.CREATED);
            }

            if (status == WorkflowStatus.SUBMITTED) {
                activities.validateOrderForSubmission(orderId);
                status = WorkflowStatus.AWAITING_APPROVAL;
                activities.updateOrderStatus(orderId, OrderStatus.AWAITING_APPROVAL);
            }

            if (status == WorkflowStatus.AWAITING_APPROVAL) {
                Workflow.await(() -> status != WorkflowStatus.AWAITING_APPROVAL);
            }

            if (status == WorkflowStatus.DECLINED) {
                activities.updateOrderStatus(orderId, OrderStatus.DECLINED);
                activities.setDeclineReason(orderId, declineReason);

                Workflow.await(() -> status != WorkflowStatus.DECLINED);
            }

            if (status == WorkflowStatus.APPROVED) {
                activities.validateOrderForApproval(orderId);
                activities.updateOrderStatus(orderId, OrderStatus.APPROVED);

                Workflow.await(() -> status == WorkflowStatus.SCHEDULED || status == WorkflowStatus.CANCELED);
            }

            if (status == WorkflowStatus.SCHEDULED) {
                activities.updateOrderStatus(orderId, OrderStatus.UNDER_DELIVERY);
                status = WorkflowStatus.UNDER_DELIVERY;

                if (scheduledDeliveryDate != null) {
                    java.time.Instant now = java.time.Instant.ofEpochMilli(Workflow.currentTimeMillis());
                    java.time.Instant deliveryInstant = scheduledDeliveryDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();

                    Duration durationUntilDelivery = Duration.between(now, deliveryInstant);

                    if (durationUntilDelivery.isPositive()) {
                        Workflow.sleep(durationUntilDelivery);
                    }
                }

                activities.fulfillDelivery(orderId);
                status = WorkflowStatus.FULFILLED;
                activities.updateOrderStatus(orderId, OrderStatus.FULFILLED);
            }

            if (status == WorkflowStatus.CANCELED) {
                activities.updateOrderStatus(orderId, OrderStatus.CANCELED);
            }
        }

        log.info("Order workflow completed for order: {} with status: {}", orderId, status);
    }

    @Override
    public void submitOrder() {
        if (status == WorkflowStatus.CREATED || status == WorkflowStatus.DECLINED) {
            status = WorkflowStatus.SUBMITTED;
        }
    }

    @Override
    public void approveOrder() {
        if (status == WorkflowStatus.AWAITING_APPROVAL) {
            status = WorkflowStatus.APPROVED;
        }
    }

    @Override
    public void declineOrder(String reason) {
        if (status == WorkflowStatus.AWAITING_APPROVAL) {
            this.declineReason = reason;
            status = WorkflowStatus.DECLINED;
        }
    }

    @Override
    public void scheduleDelivery(LocalDate deliveryDate, List<Long> truckIds, String notes) {
        if (status == WorkflowStatus.APPROVED) {
            this.scheduledDeliveryDate = deliveryDate;
            activities.validateAndScheduleDelivery(orderId, deliveryDate, truckIds, notes);
            status = WorkflowStatus.SCHEDULED;
        }
    }

    @Override
    public void cancelOrder() {
        if (status != WorkflowStatus.FULFILLED && status != WorkflowStatus.UNDER_DELIVERY && status != WorkflowStatus.CANCELED) {
            activities.cancelOrderInDB(orderId);
            status = WorkflowStatus.CANCELED;
        }
    }

    @Override
    public String updateOrderItems(com.sample.demo.dto.order.UpdateOrderItemsRequest request) {
        if (status != WorkflowStatus.CREATED && status != WorkflowStatus.DECLINED) {
            throw new IllegalStateException("Order can only be updated when status is CREATED or DECLINED. Current status: " + status);
        }

        return activities.updateOrderItemsInDB(orderId, request);
    }

    @Override
    public WorkflowStatus getStatus() {
        return status;
    }

    @Override
    public String getDeclineReason() {
        return declineReason;
    }
}
