package com.b2b.inventorysystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Integer deliveryId;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "po_id", nullable = false, unique = true)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @Column(name = "delivery_status", nullable = false, length = 50)
    private String deliveryStatus = "ASSIGNED";

    @Column(name = "signature_url", length = 255)
    private String signatureUrl;

    @Column(name = "photo_proof_url", length = 255)
    private String photoProofUrl;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
}