package com.sample.demo.repository;

import com.sample.demo.model.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Atomically decrements item quantity. Returns 0 if insufficient stock.
     * This prevents race conditions when multiple orders are scheduled simultaneously.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Item i SET i.quantity = i.quantity - :amount WHERE i.id = :id AND i.quantity >= :amount")
    int decrementQuantity(@Param("id") Long id, @Param("amount") Integer amount);
}