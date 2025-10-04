package com.sample.demo.repository;

import com.sample.demo.model.entity.Truck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TruckRepository extends JpaRepository<Truck, Long> {

    boolean existsByChassisNumber(String chassisNumber);

    boolean existsByLicensePlate(String licensePlate);
}