package com.sample.demo.repository;

import com.sample.demo.model.entity.Item;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private EntityManager entityManager;

    private Item testItem;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setItemName("Test Laptop");
        testItem.setDescription("Test laptop for inventory tests");
        testItem.setQuantity(100);
        testItem.setUnitPrice(BigDecimal.valueOf(1000));
        testItem.setPackageVolume(2.0);
        testItem.setSku("TEST-SKU-001");
        testItem = itemRepository.save(testItem);
    }

    @Test
    @Transactional
    void testDecrementQuantity_ShouldSucceed_WhenSufficientStock() {
        // Given: Item has 100 units
        int decrementAmount = 30;

        // When: Decrement by 30
        int rowsUpdated = itemRepository.decrementQuantity(testItem.getId(), decrementAmount);

        // Then: Update should succeed
        assertEquals(1, rowsUpdated, "Should update 1 row");

        // Verify the quantity was decremented atomically
        entityManager.clear();
        Item updated = itemRepository.findById(testItem.getId()).orElseThrow();
        assertEquals(70, updated.getQuantity(), "Quantity should be decremented to 70");
    }

    @Test
    @Transactional
    void testDecrementQuantity_ShouldFail_WhenInsufficientStock() {
        // Given: Item has 100 units
        int decrementAmount = 150;

        // When: Try to decrement by 150 (more than available)
        int rowsUpdated = itemRepository.decrementQuantity(testItem.getId(), decrementAmount);

        // Then: Update should fail - this prevents overselling
        assertEquals(0, rowsUpdated, "Should not update any rows when insufficient stock");

        // Verify the quantity was NOT changed
        entityManager.clear();
        Item unchanged = itemRepository.findById(testItem.getId()).orElseThrow();
        assertEquals(100, unchanged.getQuantity(), "Quantity should remain unchanged");
    }
}
