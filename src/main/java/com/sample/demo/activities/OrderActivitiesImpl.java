package com.sample.demo.activities;

import com.sample.demo.dto.order.ScheduleDeliveryRequest;
import com.sample.demo.dto.order.UpdateOrderItemsRequest;
import com.sample.demo.exception.BadRequestException;
import com.sample.demo.exception.ResourceNotFoundException;
import com.sample.demo.model.entity.*;
import com.sample.demo.model.enums.OrderStatus;
import com.sample.demo.repository.*;
import com.sample.demo.workflows.OrderWorkflow;
import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ActivityImpl(workers = OrderWorkflow.TASK_QUEUE)
@RequiredArgsConstructor
public class OrderActivitiesImpl implements OrderActivities {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final TruckRepository truckRepository;
    private final DeliveryRepository deliveryRepository;

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        log.info("Updating order {} to status {}", orderId, status);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setStatus(status);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void setDeclineReason(Long orderId, String reason) {
        log.info("Setting decline reason for order {}: {}", orderId, reason);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setDeclineReason(reason);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void validateOrderForSubmission(Long orderId) {
        log.info("Validating order {} for submission", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getOrderItems().isEmpty()) {
            throw new BadRequestException("Order must have at least one item");
        }

        order.setSubmittedDate(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void validateOrderForApproval(Long orderId) {
        log.info("Validating order {} for approval", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.AWAITING_APPROVAL) {
            throw new BadRequestException("Order must be in AWAITING_APPROVAL status");
        }
    }

    @Override
    @Transactional
    public void validateAndScheduleDelivery(Long orderId, LocalDate deliveryDate, List<Long> truckIds, String notes) {
        log.info("Scheduling delivery for order {} on {}", orderId, deliveryDate);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        validateDeliveryDate(deliveryDate);

        List<Truck> trucks = new ArrayList<>();
        for (Long truckId : truckIds) {
            Truck truck = truckRepository.findById(truckId)
                    .orElseThrow(() -> new ResourceNotFoundException("Truck", "id", truckId));
            trucks.add(truck);
        }

        validateTruckAvailability(trucks, deliveryDate);

        double totalVolume = order.getOrderItems().stream()
                .mapToDouble(OrderItem::getTotalVolume)
                .sum();

        validateTruckCapacity(trucks, totalVolume);

        Delivery delivery = new Delivery();
        delivery.setOrder(order);
        delivery.setScheduledDate(deliveryDate);
        delivery.setTrucks(trucks);
        delivery.setTotalVolume(totalVolume);
        delivery.setNotes(notes);

        order.setDelivery(delivery);
        updateInventoryQuantities(order);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void fulfillDelivery(Long orderId) {
        log.info("Fulfilling delivery for order {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getDelivery() != null) {
            order.getDelivery().setCompleted(true);
            order.getDelivery().setCompletedAt(LocalDateTime.now());
        }

        order.setStatus(OrderStatus.FULFILLED);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void cancelOrderInDB(Long orderId) {
        log.info("Canceling order {} in database", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() == OrderStatus.FULFILLED
                || order.getStatus() == OrderStatus.UNDER_DELIVERY
                || order.getStatus() == OrderStatus.CANCELED) {
            throw new BadRequestException("Order cannot be cancelled when status is FULFILLED, UNDER_DELIVERY, or CANCELED");
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }

    private void validateDeliveryDate(LocalDate date) {
        if (date.isBefore(LocalDate.now().plusDays(1))) {
            throw new BadRequestException("Delivery date must be in the future");
        }

        if (isWeekend(date)) {
            throw new BadRequestException("Deliveries cannot be scheduled on weekends");
        }
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private void validateTruckAvailability(List<Truck> trucks, LocalDate date) {
        for (Truck truck : trucks) {
            List<Delivery> existingDeliveries = deliveryRepository.findByTruckAndDate(truck.getId(), date);
            if (!existingDeliveries.isEmpty()) {
                throw new BadRequestException("Truck " + truck.getLicensePlate() + " is already scheduled for delivery on " + date);
            }
        }
    }

    private void validateTruckCapacity(List<Truck> trucks, double totalVolume) {
        double totalCapacity = trucks.stream()
                .mapToDouble(Truck::getContainerVolume)
                .sum();

        if (totalCapacity < totalVolume) {
            throw new BadRequestException("Selected trucks do not have sufficient capacity. Required: " +
                    totalVolume + ", Available: " + totalCapacity);
        }
    }

    @Override
    @Transactional
    public String updateOrderItemsInDB(Long orderId, UpdateOrderItemsRequest request) {
        log.info("Updating order items for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.DECLINED) {
            throw new BadRequestException("Order can only be updated when status is CREATED or DECLINED");
        }

        order.getOrderItems().clear();

        for (com.sample.demo.dto.order.OrderItemRequest itemRequest : request.getOrderItems()) {
            Item item = itemRepository.findById(itemRequest.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemRequest.getItemId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setRequestedQuantity(itemRequest.getRequestedQuantity());
            orderItem.setPriceAtOrder(item.getUnitPrice());

            order.addOrderItem(orderItem);
        }

        orderRepository.save(order);
        log.info("Order items updated successfully for order: {}", orderId);

        return "Order items updated successfully";
    }

    private void updateInventoryQuantities(Order order) {
        for (OrderItem orderItem : order.getOrderItems()) {
            Item item = orderItem.getItem();
            int newQuantity = item.getQuantity() - orderItem.getRequestedQuantity();

            if (newQuantity < 0) {
                throw new BadRequestException("Insufficient inventory for item: " + item.getItemName());
            }

            item.setQuantity(newQuantity);
            itemRepository.save(item);

            log.info("Updated inventory for item {}: {} -> {}",
                    item.getItemName(), item.getQuantity() + orderItem.getRequestedQuantity(), newQuantity);
        }
    }
}
