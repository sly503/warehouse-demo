package com.sample.demo.service;

import com.sample.demo.dto.item.ItemRequest;
import com.sample.demo.dto.item.ItemResponse;
import com.sample.demo.dto.item.PatchItemRequest;
import com.sample.demo.exception.BadRequestException;
import com.sample.demo.exception.ResourceNotFoundException;
import com.sample.demo.model.entity.Item;
import com.sample.demo.repository.ItemRepository;
import com.sample.demo.util.PatchUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional(readOnly = true)
    public Page<ItemResponse> getAllItems(Pageable pageable) {
        log.info("Fetching all items with pagination");
        return itemRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long id) {
        log.info("Fetching item with id: {}", id);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));
        return mapToDTO(item);
    }

    @Transactional
    public ItemResponse createItem(ItemRequest request) {
        log.info("Creating new item with name: {}", request.getItemName());

        if (request.getQuantity() < 0) {
            throw new BadRequestException("Item quantity cannot be negative");
        }

        if (request.getUnitPrice() != null && request.getUnitPrice().doubleValue() < 0) {
            throw new BadRequestException("Item unit price cannot be negative");
        }

        if (request.getPackageVolume() < 0) {
            throw new BadRequestException("Package volume cannot be negative");
        }

        Item item = new Item();
        item.setItemName(request.getItemName());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());
        item.setPackageVolume(request.getPackageVolume());
        item.setDescription(request.getDescription());
        item.setSku(request.getSku());

        Item savedItem = itemRepository.save(item);
        log.info("Item created successfully with id: {}", savedItem.getId());

        return mapToDTO(savedItem);
    }

    @Transactional
    public ItemResponse updateItem(Long id, ItemRequest request) {
        log.info("Updating item with id: {}", id);

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        if (request.getQuantity() < 0) {
            throw new BadRequestException("Item quantity cannot be negative");
        }

        if (request.getUnitPrice() != null && request.getUnitPrice().doubleValue() < 0) {
            throw new BadRequestException("Item unit price cannot be negative");
        }

        if (request.getPackageVolume() < 0) {
            throw new BadRequestException("Package volume cannot be negative");
        }

        item.setItemName(request.getItemName());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());
        item.setPackageVolume(request.getPackageVolume());
        item.setDescription(request.getDescription());
        item.setSku(request.getSku());

        Item updatedItem = itemRepository.save(item);
        log.info("Item updated successfully with id: {}", updatedItem.getId());

        return mapToDTO(updatedItem);
    }

    @Transactional
    public ItemResponse patchItem(Long id, PatchItemRequest request) {
        log.info("Partially updating item with id: {}", id);

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        PatchUtil.copyNonNullProperties(request, item);

        Item updatedItem = itemRepository.save(item);
        log.info("Item partially updated successfully with id: {}", updatedItem.getId());

        return mapToDTO(updatedItem);
    }

    @Transactional
    public void deleteItem(Long id) {
        log.info("Deleting item with id: {}", id);

        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item", "id", id);
        }

        itemRepository.deleteById(id);
        log.info("Item deleted successfully with id: {}", id);
    }

    private ItemResponse mapToDTO(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .itemName(item.getItemName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .packageVolume(item.getPackageVolume())
                .description(item.getDescription())
                .sku(item.getSku())
                .build();
    }
}