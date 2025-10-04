package com.sample.demo.repository;

import com.sample.demo.model.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByItemName(String itemName);

    List<Item> findByQuantityGreaterThan(Integer quantity);

    List<Item> findByItemNameContainingIgnoreCase(String name);

    Optional<Item> findBySku(String sku);

    @Query("SELECT i FROM Item i WHERE i.quantity < :threshold")
    List<Item> findLowStockItems(@Param("threshold") Integer threshold);
}