package com.sample.demo.activities;

import com.sample.demo.dto.order.UpdateOrderItemsRequest;
import com.sample.demo.model.enums.OrderStatus;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.time.LocalDate;
import java.util.List;

@ActivityInterface
public interface OrderActivities {

    @ActivityMethod
    void updateOrderStatus(Long orderId, OrderStatus status);

    @ActivityMethod
    void setDeclineReason(Long orderId, String reason);

    @ActivityMethod
    void validateOrderForSubmission(Long orderId);

    @ActivityMethod
    void validateOrderForApproval(Long orderId);

    @ActivityMethod
    void validateAndScheduleDelivery(Long orderId, LocalDate deliveryDate, List<Long> truckIds, String notes);

    @ActivityMethod
    void fulfillDelivery(Long orderId);

    @ActivityMethod
    void cancelOrderInDB(Long orderId);

    @ActivityMethod
    String updateOrderItemsInDB(Long orderId, UpdateOrderItemsRequest request);
}
