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

    Page<Order> findByClient(User client, Pageable pageable);

    Page<Order> findByClientAndStatus(User client, OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.submittedDate DESC NULLS LAST")
    Page<Order> findByStatusOrderBySubmittedDateDesc(@Param("status") OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o ORDER BY o.submittedDate DESC NULLS LAST")
    Page<Order> findAllOrderBySubmittedDateDesc(Pageable pageable);

}