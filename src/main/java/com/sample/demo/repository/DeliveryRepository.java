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

    List<Delivery> findByScheduledDate(LocalDate scheduledDate);

    List<Delivery> findByScheduledDateAndCompletedFalse(LocalDate scheduledDate);

    @Query("SELECT d FROM Delivery d JOIN d.trucks t WHERE t.id = :truckId AND d.scheduledDate = :date")
    List<Delivery> findByTruckAndDate(@Param("truckId") Long truckId, @Param("date") LocalDate date);
}