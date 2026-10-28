package com.b2b.inventorysystem.controller;

import com.b2b.inventorysystem.repository.InventoryRepository;
import com.b2b.inventorysystem.repository.PurchaseOrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final PurchaseOrderRepository poRepository;
    private final InventoryRepository inventoryRepository;

    public ReportController(PurchaseOrderRepository poRepository, InventoryRepository inventoryRepository) {
        this.poRepository = poRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @GetMapping("/top-items")
    public ResponseEntity<List<Map<String, Object>>> getTopItems() {
        List<Map<String, Object>> result = poRepository.findTopOrderedItems().stream()
                .map(row -> Map.of("itemName", row[0], "totalOrdered", row[1]))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stock-by-warehouse")
    public ResponseEntity<List<Map<String, Object>>> getStockByWarehouse() {
        List<Map<String, Object>> result = inventoryRepository.findStockByWarehouse().stream()
                .map(row -> Map.of("warehouseLocation", row[0], "totalStock", row[1]))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}