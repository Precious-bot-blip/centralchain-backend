package com.b2b.inventorysystem.service;

import com.b2b.inventorysystem.dto.delivery.DeliveryProofRequest;
import com.b2b.inventorysystem.dto.delivery.DeliveryResponse;
import com.b2b.inventorysystem.entity.Delivery;
import com.b2b.inventorysystem.exception.ConflictException;
import com.b2b.inventorysystem.exception.ResourceNotFoundException;
import com.b2b.inventorysystem.repository.DeliveryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final PurchaseOrderService purchaseOrderService;

    public DeliveryService(DeliveryRepository deliveryRepository,
                           PurchaseOrderService purchaseOrderService) {
        this.deliveryRepository = deliveryRepository;
        this.purchaseOrderService = purchaseOrderService;
    }

    // READ
    public List<DeliveryResponse> getAllDeliveries() {
        return deliveryRepository.findAll().stream().map(this::toResponse).toList();
    }

    public DeliveryResponse getDeliveryById(Integer id) {
        return toResponse(findDeliveryById(id));
    }

    public DeliveryResponse getDeliveryByPoId(Integer poId) {
        Delivery delivery = deliveryRepository.findByPurchaseOrderPoId(poId)
                .orElseThrow(() -> new ResourceNotFoundException("No delivery found for PO id: " + poId));
        return toResponse(delivery);
    }

    public List<DeliveryResponse> getDeliveriesByDriver(Integer driverId) {
        return deliveryRepository.findByDriverUserId(driverId).stream().map(this::toResponse).toList();
    }

    public List<DeliveryResponse> getDeliveriesByStatus(String status) {
        return deliveryRepository.findByDeliveryStatus(status).stream().map(this::toResponse).toList();
    }

    // STATE: ASSIGNED -> IN_TRANSIT
    @Transactional
    public DeliveryResponse markInTransit(Integer id) {
        Delivery delivery = findDeliveryById(id);

        if (!"ASSIGNED".equals(delivery.getDeliveryStatus())) {
            throw new ConflictException("Cannot mark IN_TRANSIT — only ASSIGNED deliveries. Current: "
                    + delivery.getDeliveryStatus());
        }

        delivery.setDeliveryStatus("IN_TRANSIT");
        return toResponse(deliveryRepository.save(delivery));
    }

    // STATE: IN_TRANSIT -> DELIVERED
    @Transactional
    public DeliveryResponse markDelivered(Integer id) {
        Delivery delivery = findDeliveryById(id);

        if (!"IN_TRANSIT".equals(delivery.getDeliveryStatus())) {
            throw new ConflictException("Cannot mark DELIVERED — only IN_TRANSIT deliveries. Current: "
                    + delivery.getDeliveryStatus());
        }

        purchaseOrderService.deliverOrder(delivery.getPurchaseOrder().getPoId());

        Delivery updated = findDeliveryById(id);
        return toResponse(updated);
    }

    // STATE: IN_TRANSIT -> FAILED
    @Transactional
    public DeliveryResponse markFailed(Integer id) {
        Delivery delivery = findDeliveryById(id);

        if (!"IN_TRANSIT".equals(delivery.getDeliveryStatus())) {
            throw new ConflictException("Cannot mark FAILED — only IN_TRANSIT deliveries. Current: "
                    + delivery.getDeliveryStatus());
        }

        delivery.setDeliveryStatus("FAILED");
        return toResponse(deliveryRepository.save(delivery));
    }

    // PROOF OF DELIVERY (attach URLs)
    @Transactional
    public DeliveryResponse attachProof(Integer id, DeliveryProofRequest request) {
        Delivery delivery = findDeliveryById(id);

        if (!"DELIVERED".equals(delivery.getDeliveryStatus())) {
            throw new ConflictException("Cannot attach proof — delivery must be DELIVERED first. Current: "
                    + delivery.getDeliveryStatus());
        }

        delivery.setSignatureUrl(request.getSignatureUrl());
        delivery.setPhotoProofUrl(request.getPhotoProofUrl());

        return toResponse(deliveryRepository.save(delivery));
    }

    // HELPERS
    private Delivery findDeliveryById(Integer id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found: " + id));
    }

    private DeliveryResponse toResponse(Delivery d) {
        return new DeliveryResponse(
                d.getDeliveryId(),
                d.getPurchaseOrder().getPoId(),
                d.getPurchaseOrder().getPoNumber(),
                d.getPurchaseOrder().getQuantity(),
                d.getPurchaseOrder().getItem().getItemId(),
                d.getPurchaseOrder().getItem().getSku(),
                d.getPurchaseOrder().getItem().getItemName(),
                d.getDriver().getUserId(),
                d.getDriver().getFullName(),
                d.getDeliveryStatus(),
                d.getPurchaseOrder().getDeliveryAddress(),
                d.getSignatureUrl(),
                d.getPhotoProofUrl(),
                d.getDeliveredAt()
        );
    }
}