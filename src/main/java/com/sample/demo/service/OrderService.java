package com.sample.demo.service;

import com.sample.demo.dto.order.*;
import com.sample.demo.exception.BadRequestException;
import com.sample.demo.exception.ResourceNotFoundException;
import com.sample.demo.model.entity.*;
import com.sample.demo.model.enums.OrderStatus;
import com.sample.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final TruckRepository truckRepository;
    private final DeliveryRepository deliveryRepository;

    // ==================== CLIENT OPERATIONS ====================

    @Transactional
    public OrderDTO createOrder(String username, CreateOrderRequest request) {
        log.info("Creating order for user: {}", username);

        User client = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Order order = new Order();
        order.setClient(client);
        order.setStatus(OrderStatus.CREATED);
        order.setDeadlineDate(request.getDeadlineDate());

        // Add order items
        for (OrderItemRequest itemRequest : request.getOrderItems()) {
            Item item = itemRepository.findById(itemRequest.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemRequest.getItemId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setRequestedQuantity(itemRequest.getRequestedQuantity());
            orderItem.setPriceAtOrder(item.getUnitPrice());

            order.addOrderItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with number: {}", savedOrder.getOrderNumber());

        return mapToDTO(savedOrder);
    }

    @Transactional
    public OrderDTO updateOrderItems(String username, Long orderId, UpdateOrderItemsRequest request) {
        log.info("Updating order items for order: {}", orderId);

        Order order = getOrderByIdAndClient(orderId, username);

        // Validate order status
        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.DECLINED) {
            throw new BadRequestException("Order can only be updated when status is CREATED or DECLINED");
        }

        // Clear existing items
        order.getOrderItems().clear();

        // Add new items
        for (OrderItemRequest itemRequest : request.getOrderItems()) {
            Item item = itemRepository.findById(itemRequest.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemRequest.getItemId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setRequestedQuantity(itemRequest.getRequestedQuantity());
            orderItem.setPriceAtOrder(item.getUnitPrice());

            order.addOrderItem(orderItem);
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Order items updated successfully for order: {}", orderId);

        return mapToDTO(updatedOrder);
    }

    @Transactional
    public OrderDTO submitOrder(String username, Long orderId) {
        log.info("Submitting order: {}", orderId);

        Order order = getOrderByIdAndClient(orderId, username);

        // Validate order status
        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.DECLINED) {
            throw new BadRequestException("Order can only be submitted when status is CREATED or DECLINED");
        }

        // Validate order has items
        if (order.getOrderItems().isEmpty()) {
            throw new BadRequestException("Order must have at least one item");
        }

        order.setStatus(OrderStatus.AWAITING_APPROVAL);
        order.setSubmittedDate(LocalDateTime.now());

        Order submittedOrder = orderRepository.save(order);
        log.info("Order submitted successfully: {}", orderId);

        return mapToDTO(submittedOrder);
    }

    @Transactional
    public void cancelOrder(String username, Long orderId) {
        log.info("Cancelling order: {}", orderId);

        Order order = getOrderByIdAndClient(orderId, username);

        // Validate order status - cannot cancel if FULFILLED, UNDER_DELIVERY, or already CANCELED
        if (order.getStatus() == OrderStatus.FULFILLED
                || order.getStatus() == OrderStatus.UNDER_DELIVERY
                || order.getStatus() == OrderStatus.CANCELED) {
            throw new BadRequestException("Order cannot be cancelled when status is FULFILLED, UNDER_DELIVERY, or CANCELED");
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);

        log.info("Order cancelled successfully: {}", orderId);
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getClientOrders(String username, OrderStatus status, Pageable pageable) {
        log.info("Fetching orders for client: {} with status: {}", username, status);

        User client = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByClientAndStatus(client, status, pageable);
        } else {
            orders = orderRepository.findByClient(client, pageable);
        }

        return orders.map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public OrderDTO getClientOrderById(String username, Long orderId) {
        log.info("Fetching order: {} for client: {}", orderId, username);
        Order order = getOrderByIdAndClient(orderId, username);
        return mapToDTO(order);
    }

    // ==================== MANAGER OPERATIONS ====================

    @Transactional(readOnly = true)
    public Page<OrderSummaryDTO> getAllOrders(OrderStatus status, Pageable pageable) {
        log.info("Fetching all orders with status: {}", status);

        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByStatusOrderBySubmittedDateDesc(status, pageable);
        } else {
            orders = orderRepository.findAllOrderBySubmittedDateDesc(pageable);
        }

        return orders.map(this::mapToSummaryDTO);
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId) {
        log.info("Fetching order details: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        return mapToDTO(order);
    }

    @Transactional
    public OrderDTO approveOrder(Long orderId) {
        log.info("Approving order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.AWAITING_APPROVAL) {
            throw new BadRequestException("Order can only be approved when status is AWAITING_APPROVAL");
        }

        order.setStatus(OrderStatus.APPROVED);
        Order approvedOrder = orderRepository.save(order);

        log.info("Order approved successfully: {}", orderId);
        return mapToDTO(approvedOrder);
    }

    @Transactional
    public OrderDTO declineOrder(Long orderId, DeclineOrderRequest request) {
        log.info("Declining order: {} with reason: {}", orderId, request.getDeclineReason());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.AWAITING_APPROVAL) {
            throw new BadRequestException("Order can only be declined when status is AWAITING_APPROVAL");
        }

        order.setStatus(OrderStatus.DECLINED);
        order.setDeclineReason(request.getDeclineReason());
        Order declinedOrder = orderRepository.save(order);

        log.info("Order declined successfully: {}", orderId);
        return mapToDTO(declinedOrder);
    }

    @Transactional
    public OrderDTO scheduleDelivery(Long orderId, ScheduleDeliveryRequest request) {
        log.info("Scheduling delivery for order: {} on date: {}", orderId, request.getScheduledDate());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED orders can be scheduled for delivery");
        }

        // Validate scheduled date
        validateDeliveryDate(request.getScheduledDate());

        // Get trucks
        List<Truck> trucks = new ArrayList<>();
        for (Long truckId : request.getTruckIds()) {
            Truck truck = truckRepository.findById(truckId)
                    .orElseThrow(() -> new ResourceNotFoundException("Truck", "id", truckId));
            trucks.add(truck);
        }

        // Validate truck availability
        validateTruckAvailability(trucks, request.getScheduledDate());

        // Calculate total volume
        double totalVolume = order.getOrderItems().stream()
                .mapToDouble(OrderItem::getTotalVolume)
                .sum();

        // Validate truck capacity
        validateTruckCapacity(trucks, totalVolume);

        // Create delivery
        Delivery delivery = new Delivery();
        delivery.setOrder(order);
        delivery.setScheduledDate(request.getScheduledDate());
        delivery.setTrucks(trucks);
        delivery.setTotalVolume(totalVolume);
        delivery.setNotes(request.getNotes());

        order.setDelivery(delivery);
        order.setStatus(OrderStatus.UNDER_DELIVERY);

        // Update inventory quantities
        updateInventoryQuantities(order);

        Order savedOrder = orderRepository.save(order);

        log.info("Delivery scheduled successfully for order: {}", orderId);
        return mapToDTO(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<LocalDate> getAvailableDeliveryDates(Long orderId, int days) {
        log.info("Fetching available delivery dates for order: {} for {} days", orderId, days);

        if (days < 3 || days > 30) {
            throw new BadRequestException("Period must be between 3 and 30 days");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED orders can be scheduled for delivery");
        }

        // Calculate total volume
        double totalVolume = order.getOrderItems().stream()
                .mapToDouble(OrderItem::getTotalVolume)
                .sum();

        List<LocalDate> availableDates = new ArrayList<>();
        LocalDate currentDate = LocalDate.now().plusDays(1);

        for (int i = 0; i < days; i++) {
            LocalDate checkDate = currentDate.plusDays(i);

            // Skip weekends
            if (isWeekend(checkDate)) {
                continue;
            }

            // Check if there are available trucks with sufficient capacity
            if (hasAvailableTrucksForDate(checkDate, totalVolume)) {
                availableDates.add(checkDate);
            }
        }

        log.info("Found {} available delivery dates", availableDates.size());
        return availableDates;
    }

    // ==================== HELPER METHODS ====================

    private Order getOrderByIdAndClient(Long orderId, String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getClient().getUsername().equals(username)) {
            throw new BadRequestException("You are not authorized to access this order");
        }

        return order;
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

    private boolean hasAvailableTrucksForDate(LocalDate date, double requiredVolume) {
        List<Truck> allTrucks = truckRepository.findAll();
        List<Delivery> scheduledDeliveries = deliveryRepository.findByScheduledDate(date);

        Set<Long> busyTruckIds = scheduledDeliveries.stream()
                .flatMap(d -> d.getTrucks().stream())
                .map(Truck::getId)
                .collect(Collectors.toSet());

        // Get available trucks
        List<Truck> availableTrucks = allTrucks.stream()
                .filter(t -> !busyTruckIds.contains(t.getId()))
                .toList();

        // Check if available trucks have sufficient capacity
        double availableCapacity = availableTrucks.stream()
                .mapToDouble(Truck::getContainerVolume)
                .sum();

        return availableCapacity >= requiredVolume;
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

    // ==================== SCHEDULED TASK ====================

    @Transactional
    public void fulfillScheduledDeliveries() {
        log.info("Running scheduled delivery fulfillment check");

        LocalDate today = LocalDate.now();
        List<Delivery> deliveriesToFulfill = deliveryRepository.findByScheduledDateAndCompletedFalse(today);

        for (Delivery delivery : deliveriesToFulfill) {
            delivery.setCompleted(true);
            delivery.setCompletedAt(LocalDateTime.now());
            delivery.getOrder().setStatus(OrderStatus.FULFILLED);

            deliveryRepository.save(delivery);
            log.info("Order {} marked as FULFILLED", delivery.getOrder().getOrderNumber());
        }

        log.info("Fulfilled {} deliveries", deliveriesToFulfill.size());
    }

    // ==================== MAPPING METHODS ====================

    private OrderDTO mapToDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .clientUsername(order.getClient().getUsername())
                .status(order.getStatus())
                .submittedDate(order.getSubmittedDate())
                .deadlineDate(order.getDeadlineDate())
                .declineReason(order.getDeclineReason())
                .orderItems(order.getOrderItems().stream()
                        .map(this::mapOrderItemToDTO)
                        .collect(Collectors.toList()))
                .delivery(order.getDelivery() != null ? mapDeliveryToDTO(order.getDelivery()) : null)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderSummaryDTO mapToSummaryDTO(Order order) {
        return OrderSummaryDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .clientUsername(order.getClient().getUsername())
                .status(order.getStatus())
                .submittedDate(order.getSubmittedDate())
                .deadlineDate(order.getDeadlineDate())
                .build();
    }

    private OrderItemDTO mapOrderItemToDTO(OrderItem orderItem) {
        return OrderItemDTO.builder()
                .id(orderItem.getId())
                .itemId(orderItem.getItem().getId())
                .itemName(orderItem.getItem().getItemName())
                .requestedQuantity(orderItem.getRequestedQuantity())
                .priceAtOrder(orderItem.getPriceAtOrder())
                .totalVolume(orderItem.getTotalVolume())
                .totalPrice(orderItem.getTotalPrice())
                .build();
    }

    private DeliveryDTO mapDeliveryToDTO(Delivery delivery) {
        return DeliveryDTO.builder()
                .id(delivery.getId())
                .scheduledDate(delivery.getScheduledDate())
                .truckIds(delivery.getTrucks().stream()
                        .map(Truck::getId)
                        .collect(Collectors.toList()))
                .totalVolume(delivery.getTotalVolume())
                .completed(delivery.isCompleted())
                .completedAt(delivery.getCompletedAt())
                .notes(delivery.getNotes())
                .build();
    }
}
