package com.sample.demo.repository;

import com.sample.demo.model.entity.Order;
import com.sample.demo.model.entity.OrderItem;
import com.sample.demo.model.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder(Order order);

    List<OrderItem> findByItem(Item item);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT SUM(oi.requestedQuantity) FROM OrderItem oi WHERE oi.item.id = :itemId")
    Integer getTotalRequestedQuantityForItem(@Param("itemId") Long itemId);
}