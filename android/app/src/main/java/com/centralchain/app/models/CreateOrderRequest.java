package com.centralchain.app.models;

public class CreateOrderRequest {
    public long itemId;
    public int quantity;
    public long createdByUserId;
    public long assignedSupplierId;

    public CreateOrderRequest(long itemId, int quantity, long createdByUserId, long assignedSupplierId) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.createdByUserId = createdByUserId;
        this.assignedSupplierId = assignedSupplierId;
    }
}
