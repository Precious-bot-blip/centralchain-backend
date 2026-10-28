package com.b2b.inventorysystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "po_id")
    private Integer poId;

    @Column(name = "po_number", nullable = false, unique = true, length = 50)
    private String poNumber;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Inventory item;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING";

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "assigned_supplier_id", nullable = false)
    private User assignedSupplier;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_driver_id")
    private User assignedDriver;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}