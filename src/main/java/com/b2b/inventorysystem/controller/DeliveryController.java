package com.b2b.inventorysystem.controller;

import com.b2b.inventorysystem.dto.delivery.DeliveryProofRequest;
import com.b2b.inventorysystem.dto.delivery.DeliveryResponse;
import com.b2b.inventorysystem.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    // READ
    @GetMapping
    public ResponseEntity<List<DeliveryResponse>> getAllDeliveries() {
        return ResponseEntity.ok(deliveryService.getAllDeliveries());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> getDeliveryById(@PathVariable Integer id) {
        return ResponseEntity.ok(deliveryService.getDeliveryById(id));
    }

    @GetMapping("/po/{poId}")
    public ResponseEntity<DeliveryResponse> getDeliveryByPoId(@PathVariable Integer poId) {
        return ResponseEntity.ok(deliveryService.getDeliveryByPoId(poId));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByDriver(@PathVariable Integer driverId) {
        return ResponseEntity.ok(deliveryService.getDeliveriesByDriver(driverId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(deliveryService.getDeliveriesByStatus(status));
    }

    // STATE TRANSITIONS
    @PatchMapping("/{id}/in-transit")
    public ResponseEntity<DeliveryResponse> markInTransit(@PathVariable Integer id) {
        return ResponseEntity.ok(deliveryService.markInTransit(id));
    }

    @PatchMapping("/{id}/delivered")
    public ResponseEntity<DeliveryResponse> markDelivered(@PathVariable Integer id) {
        return ResponseEntity.ok(deliveryService.markDelivered(id));
    }

    @PatchMapping("/{id}/failed")
    public ResponseEntity<DeliveryResponse> markFailed(@PathVariable Integer id) {
        return ResponseEntity.ok(deliveryService.markFailed(id));
    }

    // PROOF OF DELIVERY
    @PatchMapping("/{id}/proof")
    public ResponseEntity<DeliveryResponse> attachProof(@PathVariable Integer id,
                                                        @Valid @RequestBody DeliveryProofRequest request) {
        return ResponseEntity.ok(deliveryService.attachProof(id, request));
    }
}