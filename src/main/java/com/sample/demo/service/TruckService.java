package com.sample.demo.service;

import com.sample.demo.dto.truck.CreateTruckRequest;
import com.sample.demo.dto.truck.TruckDTO;
import com.sample.demo.exception.BadRequestException;
import com.sample.demo.exception.DuplicateResourceException;
import com.sample.demo.exception.ResourceNotFoundException;
import com.sample.demo.model.entity.Truck;
import com.sample.demo.repository.TruckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TruckService {

    private final TruckRepository truckRepository;

    @Transactional(readOnly = true)
    public Page<TruckDTO> getAllTrucks(Pageable pageable) {
        log.info("Fetching all trucks with pagination");
        return truckRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public TruckDTO getTruckById(Long id) {
        log.info("Fetching truck with id: {}", id);
        Truck truck = truckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Truck", "id", id));
        return mapToDTO(truck);
    }

    @Transactional
    public TruckDTO createTruck(CreateTruckRequest request) {
        log.info("Creating new truck with chassis number: {}", request.getChassisNumber());

        // Validate container volume
        if (request.getContainerVolume() <= 0) {
            throw new BadRequestException("Container volume must be greater than 0");
        }

        // Check for duplicate chassis number
        if (truckRepository.existsByChassisNumber(request.getChassisNumber())) {
            throw new DuplicateResourceException("Truck", "chassisNumber", request.getChassisNumber());
        }

        // Check for duplicate license plate
        if (truckRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new DuplicateResourceException("Truck", "licensePlate", request.getLicensePlate());
        }

        Truck truck = new Truck();
        truck.setChassisNumber(request.getChassisNumber());
        truck.setLicensePlate(request.getLicensePlate());
        truck.setContainerVolume(request.getContainerVolume());

        Truck savedTruck = truckRepository.save(truck);
        log.info("Truck created successfully with id: {}", savedTruck.getId());

        return mapToDTO(savedTruck);
    }

    @Transactional
    public TruckDTO updateTruck(Long id, CreateTruckRequest request) {
        log.info("Updating truck with id: {}", id);

        Truck truck = truckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Truck", "id", id));

        // Validate container volume
        if (request.getContainerVolume() <= 0) {
            throw new BadRequestException("Container volume must be greater than 0");
        }

        // Check for duplicate chassis number (if changed)
        if (!truck.getChassisNumber().equals(request.getChassisNumber())
                && truckRepository.existsByChassisNumber(request.getChassisNumber())) {
            throw new DuplicateResourceException("Truck", "chassisNumber", request.getChassisNumber());
        }

        // Check for duplicate license plate (if changed)
        if (!truck.getLicensePlate().equals(request.getLicensePlate())
                && truckRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new DuplicateResourceException("Truck", "licensePlate", request.getLicensePlate());
        }

        truck.setChassisNumber(request.getChassisNumber());
        truck.setLicensePlate(request.getLicensePlate());
        truck.setContainerVolume(request.getContainerVolume());

        Truck updatedTruck = truckRepository.save(truck);
        log.info("Truck updated successfully with id: {}", updatedTruck.getId());

        return mapToDTO(updatedTruck);
    }

    @Transactional
    public void deleteTruck(Long id) {
        log.info("Deleting truck with id: {}", id);

        if (!truckRepository.existsById(id)) {
            throw new ResourceNotFoundException("Truck", "id", id);
        }

        truckRepository.deleteById(id);
        log.info("Truck deleted successfully with id: {}", id);
    }

    private TruckDTO mapToDTO(Truck truck) {
        return TruckDTO.builder()
                .id(truck.getId())
                .chassisNumber(truck.getChassisNumber())
                .licensePlate(truck.getLicensePlate())
                .containerVolume(truck.getContainerVolume())
                .build();
    }
}