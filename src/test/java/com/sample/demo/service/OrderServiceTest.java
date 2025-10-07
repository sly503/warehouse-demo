package com.sample.demo.service;

import com.sample.demo.dto.order.OrderDTO;
import com.sample.demo.dto.order.OrderWarning;
import com.sample.demo.model.entity.*;
import com.sample.demo.model.enums.OrderStatus;
import com.sample.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TruckRepository truckRepository;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private SystemConfigService configService;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private Item item;
    private List<Truck> allTrucks;

    @BeforeEach
    void setUp() {
        User client = new User();
        client.setId(1L);
        client.setUsername("client1");

        item = new Item();
        item.setId(1L);
        item.setItemName("Laptop");
        item.setQuantity(100);
        item.setPackageVolume(2.0);

        Truck truck1 = new Truck();
        truck1.setId(1L);
        truck1.setLicensePlate("TR-001");
        truck1.setContainerVolume(100.0);

        Truck truck2 = new Truck();
        truck2.setId(2L);
        truck2.setLicensePlate("TR-002");
        truck2.setContainerVolume(150.0);

        allTrucks = new ArrayList<>();
        allTrucks.add(truck1);
        allTrucks.add(truck2);

        order = new Order();
        order.setId(1L);
        order.setOrderNumber("ORD-001");
        order.setClient(client);
        order.setStatus(OrderStatus.APPROVED);
        order.setDeadlineDate(LocalDate.now().plusDays(2));
        order.setSubmittedDate(LocalDateTime.now());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setOrder(order);
        orderItem.setItem(item);
        orderItem.setRequestedQuantity(125);
        orderItem.setPriceAtOrder(BigDecimal.valueOf(1000));
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        order.setOrderItems(orderItems);
    }

    @Test
    void testGetOrderById_WhenNoDeliverySlots_ShouldShowWarning() {
        // Given: Order needs 250 volume, trucks have 250 total capacity but all are scheduled
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(truckRepository.findAll()).thenReturn(allTrucks);
        when(configService.getDeliveryPeriod()).thenReturn(7);

        // Mock that all trucks are busy for all available dates
        List<Delivery> scheduledDeliveries = new ArrayList<>();
        Delivery delivery1 = new Delivery();
        delivery1.setTrucks(allTrucks);
        scheduledDeliveries.add(delivery1);
        when(deliveryRepository.findByScheduledDate(any(LocalDate.class))).thenReturn(scheduledDeliveries);

        // When: Manager gets order details
        OrderDTO result = orderService.getOrderById(1L);

        // Then: Should have NO_DELIVERY_SLOTS warning
        assertNotNull(result);
        assertNotNull(result.getWarnings());

        boolean hasNoDeliverySlotsWarning = result.getWarnings().stream()
                .anyMatch(w -> "NO_DELIVERY_SLOTS".equals(w.getType()));

        assertTrue(hasNoDeliverySlotsWarning, "Should have NO_DELIVERY_SLOTS warning");

        OrderWarning warning = result.getWarnings().stream()
                .filter(w -> "NO_DELIVERY_SLOTS".equals(w.getType()))
                .findFirst()
                .orElseThrow();

        assertEquals("ERROR", warning.getSeverity());
        assertTrue(warning.getMessage().contains("No available delivery slots"));
    }

    @Test
    void testGetOrderById_WhenCapacityExceeded_ShouldShowWarning() {
        // Given: Order needs 300 volume, trucks have only 250 total capacity
        item.setPackageVolume(2.0);
        OrderItem orderItem = order.getOrderItems().getFirst();
        orderItem.setRequestedQuantity(150);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(truckRepository.findAll()).thenReturn(allTrucks);
        when(configService.getDeliveryPeriod()).thenReturn(7);

        // When: Manager gets order details
        OrderDTO result = orderService.getOrderById(1L);

        // Then: Should have INSUFFICIENT_CAPACITY and NO_DELIVERY_SLOTS warnings
        assertNotNull(result);
        assertNotNull(result.getWarnings());

        boolean hasInsufficientCapacity = result.getWarnings().stream()
                .anyMatch(w -> "INSUFFICIENT_CAPACITY".equals(w.getType()));

        boolean hasNoDeliverySlots = result.getWarnings().stream()
                .anyMatch(w -> "NO_DELIVERY_SLOTS".equals(w.getType()));

        assertTrue(hasInsufficientCapacity, "Should have INSUFFICIENT_CAPACITY warning");
        assertTrue(hasNoDeliverySlots, "Should have NO_DELIVERY_SLOTS warning");
    }

    @Test
    void testGetOrderById_WhenTrucksAvailable_ShouldNotShowNoDeliverySlotsWarning() {
        // Given: Order needs 250 volume, trucks have 250 capacity and some are available
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(truckRepository.findAll()).thenReturn(allTrucks);
        when(configService.getDeliveryPeriod()).thenReturn(7);

        // Mock that no trucks are scheduled (empty list)
        when(deliveryRepository.findByScheduledDate(any(LocalDate.class))).thenReturn(new ArrayList<>());

        // When: Manager gets order details
        OrderDTO result = orderService.getOrderById(1L);

        // Then: Should NOT have NO_DELIVERY_SLOTS warning
        assertNotNull(result);
        assertNotNull(result.getWarnings());

        boolean hasNoDeliverySlotsWarning = result.getWarnings().stream()
                .anyMatch(w -> "NO_DELIVERY_SLOTS".equals(w.getType()));

        assertFalse(hasNoDeliverySlotsWarning, "Should NOT have NO_DELIVERY_SLOTS warning when trucks are available");
    }

    @Test
    void testGetOrderById_WhenInsufficientInventory_ShouldShowWarning() {
        // Given: Order requests 125 items but only 100 available
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(truckRepository.findAll()).thenReturn(allTrucks);
        when(configService.getDeliveryPeriod()).thenReturn(7);
        when(deliveryRepository.findByScheduledDate(any(LocalDate.class))).thenReturn(new ArrayList<>());

        // When: Manager gets order details
        OrderDTO result = orderService.getOrderById(1L);

        // Then: Should have INSUFFICIENT_INVENTORY warning
        assertNotNull(result);
        assertNotNull(result.getWarnings());

        boolean hasInsufficientInventory = result.getWarnings().stream()
                .anyMatch(w -> "INSUFFICIENT_INVENTORY".equals(w.getType()));

        assertTrue(hasInsufficientInventory, "Should have INSUFFICIENT_INVENTORY warning");

        OrderWarning warning = result.getWarnings().stream()
                .filter(w -> "INSUFFICIENT_INVENTORY".equals(w.getType()))
                .findFirst()
                .orElseThrow();

        assertEquals("WARNING", warning.getSeverity());
        assertTrue(warning.getMessage().contains("Laptop"));
        assertTrue(warning.getMessage().contains("125"));
        assertTrue(warning.getMessage().contains("100"));
    }

    @Test
    void testGetOrderById_WhenDeadlinePassed_ShouldShowWarning() {
        // Given: Order has deadline in the past
        order.setDeadlineDate(LocalDate.now().minusDays(1));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(truckRepository.findAll()).thenReturn(allTrucks);
        when(configService.getDeliveryPeriod()).thenReturn(7);

        // When: Manager gets order details
        OrderDTO result = orderService.getOrderById(1L);

        // Then: Should have DEADLINE_PASSED warning
        assertNotNull(result);
        assertNotNull(result.getWarnings());

        boolean hasDeadlinePassed = result.getWarnings().stream()
                .anyMatch(w -> "DEADLINE_PASSED".equals(w.getType()));

        assertTrue(hasDeadlinePassed, "Should have DEADLINE_PASSED warning");

        OrderWarning warning = result.getWarnings().stream()
                .filter(w -> "DEADLINE_PASSED".equals(w.getType()))
                .findFirst()
                .orElseThrow();

        assertEquals("ERROR", warning.getSeverity());
    }

    @Test
    void testGetOrderById_WhenMultipleTrucksRequired_ShouldShowInfo() {
        // Given: Order needs 200 volume, largest truck is 150
        item.setPackageVolume(2.0);
        OrderItem orderItem = order.getOrderItems().getFirst();
        orderItem.setRequestedQuantity(100);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(truckRepository.findAll()).thenReturn(allTrucks);
        when(configService.getDeliveryPeriod()).thenReturn(7);
        when(deliveryRepository.findByScheduledDate(any(LocalDate.class))).thenReturn(new ArrayList<>());

        OrderDTO result = orderService.getOrderById(1L);

        // Then: Should have MULTIPLE_TRUCKS_REQUIRED info
        assertNotNull(result);
        assertNotNull(result.getWarnings());

        boolean hasMultipleTrucks = result.getWarnings().stream()
                .anyMatch(w -> "MULTIPLE_TRUCKS_REQUIRED".equals(w.getType()));

        assertTrue(hasMultipleTrucks, "Should have MULTIPLE_TRUCKS_REQUIRED info");

        OrderWarning warning = result.getWarnings().stream()
                .filter(w -> "MULTIPLE_TRUCKS_REQUIRED".equals(w.getType()))
                .findFirst()
                .orElseThrow();

        assertEquals("INFO", warning.getSeverity());
    }
}
