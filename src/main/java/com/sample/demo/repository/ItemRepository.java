package com.sample.demo.repository;

import com.sample.demo.model.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    // Basic CRUD operations are inherited from JpaRepository
}