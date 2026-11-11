package com.centralchain.app.models;

// for editing a pending order
public class UpdateOrderRequest {
    public int quantity;
    public long assignedSupplierId;

    public UpdateOrderRequest(int quantity, long assignedSupplierId) {
        this.quantity = quantity;
        this.assignedSupplierId = assignedSupplierId;
    }
}
