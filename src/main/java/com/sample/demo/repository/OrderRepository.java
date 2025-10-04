package com.sample.demo.repository;

import com.sample.demo.model.entity.Order;
import com.sample.demo.model.entity.User;
import com.sample.demo.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByClient(User client);

    List<Order> findByClientAndStatus(User client, OrderStatus status);

    Page<Order> findByClient(User client, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByClientAndStatus(User client, OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.submittedDate DESC NULLS LAST")
    Page<Order> findByStatusOrderBySubmittedDateDesc(@Param("status") OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o ORDER BY o.submittedDate DESC NULLS LAST")
    Page<Order> findAllOrderBySubmittedDateDesc(Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status IN :statuses")
    List<Order> findByStatusIn(@Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.deadlineDate <= :date AND o.status = :status")
    List<Order> findByDeadlineDateBeforeAndStatus(@Param("date") LocalDate date, @Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.status = 'APPROVED' AND o.delivery IS NULL")
    List<Order> findApprovedOrdersWithoutDelivery();

    @Query("SELECT o FROM Order o JOIN o.delivery d WHERE d.scheduledDate = :date AND d.completed = false")
    List<Order> findOrdersScheduledForDelivery(@Param("date") LocalDate date);
}