package com.b2b.inventorysystem.service;

import com.b2b.inventorysystem.dto.inventory.InventoryCreateRequest;
import com.b2b.inventorysystem.dto.inventory.InventoryResponse;
import com.b2b.inventorysystem.dto.inventory.InventoryUpdateRequest;
import com.b2b.inventorysystem.entity.Inventory;
import com.b2b.inventorysystem.exception.BadRequestException;
import com.b2b.inventorysystem.exception.ConflictException;
import com.b2b.inventorysystem.exception.ResourceNotFoundException;
import com.b2b.inventorysystem.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    // READ
    public List<InventoryResponse> getAllItems() {
        return inventoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public InventoryResponse getItemById(Integer id) {
        Inventory item = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with id: " + id));
        return toResponse(item);
    }

    public InventoryResponse getItemBySku(String sku) {
        Inventory item = inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with SKU: " + sku));
        return toResponse(item);
    }

    public List<InventoryResponse> getItemsByLocation(String location) {
        return inventoryRepository.findByWarehouseLocation(location)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<InventoryResponse> getLowStockItems() {
        return inventoryRepository.findLowStockItems()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public long countItems() {
        return inventoryRepository.count();
    }

    // CREATE
    public InventoryResponse createItem(InventoryCreateRequest request) {
        if (inventoryRepository.existsBySku(request.getSku())) {
            throw new ConflictException("SKU already exists: " + request.getSku());
        }

        Inventory item = new Inventory();
        item.setSku(request.getSku());
        item.setItemName(request.getItemName());
        item.setWarehouseLocation(request.getWarehouseLocation());
        item.setQuantityInStock(request.getQuantityInStock());
        item.setMinSafetyThreshold(request.getMinSafetyThreshold());
        item.setUnitPrice(request.getUnitPrice());

        Inventory saved = inventoryRepository.save(item);
        return toResponse(saved);
    }

    // UPDATE
    public InventoryResponse updateItem(Integer id, InventoryUpdateRequest request) {
        Inventory item = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with id: " + id));

        item.setItemName(request.getItemName());
        item.setWarehouseLocation(request.getWarehouseLocation());
        item.setMinSafetyThreshold(request.getMinSafetyThreshold());
        item.setUnitPrice(request.getUnitPrice());

        Inventory updated = inventoryRepository.save(item);
        return toResponse(updated);
    }

    // ADJUST STOCK
    public InventoryResponse adjustStock(Integer id, Integer delta) {
        Inventory item = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with id: " + id));

        int newQuantity = item.getQuantityInStock() + delta;

        if (newQuantity < 0) {
            throw new BadRequestException(
                    "Stock adjustment would result in negative quantity. Current: "
                            + item.getQuantityInStock() + ", Delta: " + delta);
        }

        item.setQuantityInStock(newQuantity);
        Inventory updated = inventoryRepository.save(item);
        return toResponse(updated);
    }

    // DELETE
    public void deleteItem(Integer id) {
        Inventory item = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with id: " + id));

        try {
            inventoryRepository.delete(item);
        } catch (Exception ex) {
            throw new ConflictException(
                    "Cannot delete item — it has references in purchase orders");
        }
    }

    // HELPER
    private InventoryResponse toResponse(Inventory item) {
        return new InventoryResponse(
                item.getItemId(),
                item.getSku(),
                item.getItemName(),
                item.getWarehouseLocation(),
                item.getQuantityInStock(),
                item.getMinSafetyThreshold(),
                item.getUnitPrice(),
                item.getUpdatedAt()
        );
    }
}