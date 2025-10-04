package com.sample.demo.repository;

import com.sample.demo.model.entity.Delivery;
import com.sample.demo.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByOrder(Order order);

    List<Delivery> findByScheduledDate(LocalDate scheduledDate);

    List<Delivery> findByScheduledDateAndCompletedFalse(LocalDate scheduledDate);

    List<Delivery> findByCompleted(boolean completed);

    @Query("SELECT d FROM Delivery d WHERE d.scheduledDate BETWEEN :startDate AND :endDate")
    List<Delivery> findDeliveriesBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT d FROM Delivery d WHERE d.scheduledDate <= :date AND d.completed = false")
    List<Delivery> findPendingDeliveriesBeforeDate(@Param("date") LocalDate date);

    @Query("SELECT d FROM Delivery d JOIN d.trucks t WHERE t.id = :truckId AND d.scheduledDate = :date")
    List<Delivery> findByTruckAndDate(@Param("truckId") Long truckId, @Param("date") LocalDate date);
}