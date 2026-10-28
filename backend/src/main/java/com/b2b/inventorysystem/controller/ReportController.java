package com.b2b.inventorysystem.controller;

import com.b2b.inventorysystem.entity.User;
import com.b2b.inventorysystem.repository.InventoryRepository;
import com.b2b.inventorysystem.repository.PurchaseOrderRepository;
import com.b2b.inventorysystem.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    private final UserRepository userRepository;

    public ReportController(PurchaseOrderRepository poRepository, InventoryRepository inventoryRepository, UserRepository userRepository) {
        this.poRepository = poRepository;
        this.inventoryRepository = inventoryRepository;
        this.userRepository = userRepository;
    }

    private Integer getLoggedInUserId(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getUserId();
    }

    @GetMapping("/top-items")
    public ResponseEntity<List<Map<String, Object>>> getTopItems(Authentication auth) {
        Integer supplierId = getLoggedInUserId(auth);
        List<Map<String, Object>> result = poRepository.findTopOrderedItemsBySupplier(supplierId).stream()
                .map(row -> Map.of("itemName", row[0], "totalOrdered", row[1]))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stock-by-warehouse")
    public ResponseEntity<List<Map<String, Object>>> getStockByWarehouse(Authentication auth) {
        Integer supplierId = getLoggedInUserId(auth);
        List<Map<String, Object>> result = inventoryRepository.findStockByWarehouseForSupplier(supplierId).stream()
                .map(row -> Map.of("warehouseLocation", row[0], "totalStock", row[1]))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}