package com.b2b.inventorysystem.controller;

import com.b2b.inventorysystem.dto.inventory.InventoryCreateRequest;
import com.b2b.inventorysystem.dto.inventory.InventoryResponse;
import com.b2b.inventorysystem.dto.inventory.InventoryUpdateRequest;
import com.b2b.inventorysystem.repository.UserRepository;
import com.b2b.inventorysystem.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    public InventoryController(InventoryService inventoryService, UserRepository userRepository) {
        this.inventoryService = inventoryService;
        this.userRepository = userRepository;
    }

    // READ
    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllItems(Authentication auth) {
        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        Integer userId = userRepository.findByEmail(auth.getName()).get().getUserId();
        return ResponseEntity.ok(inventoryService.getAllItems(userId, role));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponse> getItemById(@PathVariable Integer id) {
        return ResponseEntity.ok(inventoryService.getItemById(id));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<InventoryResponse> getItemBySku(@PathVariable String sku) {
        return ResponseEntity.ok(inventoryService.getItemBySku(sku));
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<List<InventoryResponse>> getItemsByLocation(@PathVariable String location) {
        return ResponseEntity.ok(inventoryService.getItemsByLocation(location));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryResponse>> getLowStockItems() {
        return ResponseEntity.ok(inventoryService.getLowStockItems());
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countItems() {
        return ResponseEntity.ok(inventoryService.countItems());
    }

    // CREATE
    @PostMapping
    public ResponseEntity<InventoryResponse> createItem(@Valid @RequestBody InventoryCreateRequest request) {
        InventoryResponse response = inventoryService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<InventoryResponse> updateItem(@PathVariable Integer id,
                                                        @Valid @RequestBody InventoryUpdateRequest request) {
        return ResponseEntity.ok(inventoryService.updateItem(id, request));
    }

    // ADJUST STOCK
    @PatchMapping("/{id}/adjust")
    public ResponseEntity<InventoryResponse> adjustStock(@PathVariable Integer id,
                                                         @RequestParam Integer delta) {
        return ResponseEntity.ok(inventoryService.adjustStock(id, delta));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Integer id) {
        inventoryService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}