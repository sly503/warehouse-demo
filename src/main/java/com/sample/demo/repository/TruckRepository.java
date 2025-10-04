package com.sample.demo.repository;

import com.sample.demo.model.entity.Truck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TruckRepository extends JpaRepository<Truck, Long> {

    Optional<Truck> findByChassisNumber(String chassisNumber);

    Optional<Truck> findByLicensePlate(String licensePlate);

    boolean existsByChassisNumber(String chassisNumber);

    boolean existsByLicensePlate(String licensePlate);

    List<Truck> findByAvailable(boolean available);

    @Query("SELECT t FROM Truck t WHERE t.containerVolume >= :requiredVolume AND t.available = true")
    List<Truck> findAvailableTrucksWithMinVolume(@Param("requiredVolume") Double requiredVolume);

    @Query("SELECT t FROM Truck t WHERE t.id NOT IN " +
           "(SELECT dt.id FROM Delivery d JOIN d.trucks dt WHERE d.scheduledDate = :date)")
    List<Truck> findTrucksAvailableOnDate(@Param("date") LocalDate date);
}