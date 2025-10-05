package com.sample.demo.workflows;

import com.sample.demo.dto.order.UpdateOrderItemsRequest;
import com.sample.demo.model.enums.WorkflowStatus;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.time.LocalDate;
import java.util.List;

@WorkflowInterface
public interface OrderWorkflow {

    String TASK_QUEUE = "order-workflow-worker";

    @WorkflowMethod
    void processOrder(Long orderId, String clientUsername);

    @SignalMethod
    void submitOrder();

    @SignalMethod
    void approveOrder();

    @SignalMethod
    void declineOrder(String reason);

    @SignalMethod
    void scheduleDelivery(LocalDate deliveryDate, List<Long> truckIds, String notes);

    @SignalMethod
    void cancelOrder();

    @UpdateMethod
    String updateOrderItems(UpdateOrderItemsRequest request);

    @QueryMethod
    WorkflowStatus getStatus();

    @QueryMethod
    String getDeclineReason();
}
