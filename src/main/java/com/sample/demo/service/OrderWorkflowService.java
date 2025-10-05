package com.sample.demo.service;

import com.sample.demo.model.enums.WorkflowStatus;
import com.sample.demo.workflows.OrderWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderWorkflowService {

    private final WorkflowClient workflowClient;

    public String startOrderWorkflow(Long orderId, String clientUsername) {
        String workflowId = "order-workflow-" + orderId;

        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setWorkflowId(workflowId)
                .setTaskQueue(OrderWorkflow.TASK_QUEUE)
                .setWorkflowExecutionTimeout(Duration.ofDays(30))
                .build();

        OrderWorkflow workflow = workflowClient.newWorkflowStub(OrderWorkflow.class, options);

        WorkflowClient.start(workflow::processOrder, orderId, clientUsername);

        log.info("Started order workflow with ID: {}", workflowId);
        return workflowId;
    }

    public void submitOrder(Long orderId) {
        String workflowId = "order-workflow-" + orderId;
        OrderWorkflow workflow = workflowClient.newWorkflowStub(OrderWorkflow.class, workflowId);
        workflow.submitOrder();
        log.info("Submitted signal to workflow: {}", workflowId);
    }

    public void approveOrder(Long orderId) {
        String workflowId = "order-workflow-" + orderId;
        OrderWorkflow workflow = workflowClient.newWorkflowStub(OrderWorkflow.class, workflowId);
        workflow.approveOrder();
        log.info("Approved signal sent to workflow: {}", workflowId);
    }

    public void declineOrder(Long orderId, String reason) {
        String workflowId = "order-workflow-" + orderId;
        OrderWorkflow workflow = workflowClient.newWorkflowStub(OrderWorkflow.class, workflowId);
        workflow.declineOrder(reason);
        log.info("Declined signal sent to workflow: {}", workflowId);
    }

    public void scheduleDelivery(Long orderId, LocalDate deliveryDate, List<Long> truckIds, String notes) {
        String workflowId = "order-workflow-" + orderId;
        OrderWorkflow workflow = workflowClient.newWorkflowStub(OrderWorkflow.class, workflowId);
        workflow.scheduleDelivery(deliveryDate, truckIds, notes);
        log.info("Delivery scheduled signal sent to workflow: {}", workflowId);
    }

    public void cancelOrder(Long orderId) {
        String workflowId = "order-workflow-" + orderId;
        OrderWorkflow workflow = workflowClient.newWorkflowStub(OrderWorkflow.class, workflowId);
        workflow.cancelOrder();
        log.info("Cancel signal sent to workflow: {}", workflowId);
    }

    public String updateOrderItems(Long orderId, com.sample.demo.dto.order.UpdateOrderItemsRequest request) {
        String workflowId = "order-workflow-" + orderId;
        OrderWorkflow workflow = workflowClient.newWorkflowStub(OrderWorkflow.class, workflowId);
        String result = workflow.updateOrderItems(request);
        log.info("Update request sent to workflow: {}", workflowId);
        return result;
    }

    public WorkflowStatus getWorkflowStatus(Long orderId) {
        String workflowId = "order-workflow-" + orderId;
        try {
            OrderWorkflow workflow = workflowClient.newWorkflowStub(OrderWorkflow.class, workflowId);
            return workflow.getStatus();
        } catch (Exception e) {
            log.error("Failed to get workflow status for order: {}", orderId, e);
            return null;
        }
    }

    public String getDeclineReason(Long orderId) {
        String workflowId = "order-workflow-" + orderId;
        try {
            OrderWorkflow workflow = workflowClient.newWorkflowStub(OrderWorkflow.class, workflowId);
            return workflow.getDeclineReason();
        } catch (Exception e) {
            log.error("Failed to get decline reason for order: {}", orderId, e);
            return null;
        }
    }

    public boolean isWorkflowRunning(Long orderId) {
        String workflowId = "order-workflow-" + orderId;
        try {
            WorkflowStub workflowStub = workflowClient.newUntypedWorkflowStub(workflowId);
            return !workflowStub.getResult(String.class).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
