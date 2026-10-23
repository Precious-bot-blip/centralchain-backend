package com.b2b.inventorysystem.controller;

import com.b2b.inventorysystem.dto.purchaseorder.PurchaseOrderCreateRequest;
import com.b2b.inventorysystem.dto.purchaseorder.PurchaseOrderResponse;
import com.b2b.inventorysystem.dto.purchaseorder.PurchaseOrderUpdateRequest;
import com.b2b.inventorysystem.repository.UserRepository;
import com.b2b.inventorysystem.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService poService;
    private final UserRepository userRepository;

    public PurchaseOrderController(PurchaseOrderService poService, UserRepository userRepository) {
        this.poService = poService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrderResponse>> getAllOrders(Authentication auth) {
        String role = extractRoleFromAuth(auth);
        Integer userId = extractUserIdFromAuth(auth);
        return ResponseEntity.ok(poService.getAllOrders(userId, role));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> getOrderById(@PathVariable Integer id) {
        return ResponseEntity.ok(poService.getOrderById(id));
    }

    @GetMapping("/number/{poNumber}")
    public ResponseEntity<PurchaseOrderResponse> getOrderByNumber(@PathVariable String poNumber) {
        return ResponseEntity.ok(poService.getOrderByNumber(poNumber));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PurchaseOrderResponse>> getOrdersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(poService.getOrdersByStatus(status));
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<PurchaseOrderResponse>> getOrdersBySupplier(@PathVariable Integer supplierId) {
        return ResponseEntity.ok(poService.getOrdersBySupplier(supplierId));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<PurchaseOrderResponse>> getOrdersByDriver(@PathVariable Integer driverId) {
        return ResponseEntity.ok(poService.getOrdersByDriver(driverId));
    }

    @GetMapping("/creator/{userId}")
    public ResponseEntity<List<PurchaseOrderResponse>> getOrdersByCreator(@PathVariable Integer userId) {
        return ResponseEntity.ok(poService.getOrdersByCreator(userId));
    }

    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> createOrder(@Valid @RequestBody PurchaseOrderCreateRequest request) {
        PurchaseOrderResponse response = poService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> updateOrder(@PathVariable Integer id,
                                                             @Valid @RequestBody PurchaseOrderUpdateRequest request) {
        return ResponseEntity.ok(poService.updateOrder(id, request));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<PurchaseOrderResponse> approveOrder(@PathVariable Integer id) {
        return ResponseEntity.ok(poService.approveOrder(id));
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<PurchaseOrderResponse> acceptOrder(@PathVariable Integer id, Authentication auth) {
        Integer supplierId = extractUserIdFromAuth(auth);
        return ResponseEntity.ok(poService.acceptOrder(id, supplierId));
    }

    @PatchMapping("/{id}/dispatch")
    public ResponseEntity<PurchaseOrderResponse> dispatchOrder(@PathVariable Integer id,
                                                               @RequestParam Integer driverId) {
        return ResponseEntity.ok(poService.dispatchOrder(id, driverId));
    }

    @PatchMapping("/{id}/deliver")
    public ResponseEntity<PurchaseOrderResponse> deliverOrder(@PathVariable Integer id) {
        return ResponseEntity.ok(poService.deliverOrder(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PurchaseOrderResponse> cancelOrder(@PathVariable Integer id) {
        return ResponseEntity.ok(poService.cancelOrder(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer id) {
        poService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    // HELPERS
    private String extractRoleFromAuth(Authentication auth) {
        return auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("UNKNOWN");
    }

    private Integer extractUserIdFromAuth(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .map(u -> u.getUserId())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}