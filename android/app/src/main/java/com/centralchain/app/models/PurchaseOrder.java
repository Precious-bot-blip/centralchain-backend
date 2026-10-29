package com.centralchain.app.models;

public class PurchaseOrder {
    // order status values
    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String PROCESSING = "PROCESSING";
    public static final String DISPATCHED = "DISPATCHED";
    public static final String DELIVERED = "DELIVERED";
    public static final String CANCELLED = "CANCELLED";

    public long poId;
    public String poNumber;
    public long itemId;
    public String itemSku;
    public String itemName;
    public int quantity;
    public String deliveryAddress;
    public String status;
    public long createdByUserId;
    public String createdByUserName;
    public long assignedSupplierId;
    public String assignedSupplierName;
    public Long assignedDriverId;
    public String assignedDriverName;
    public String createdAt;

    // not delivered or cancelled
    public boolean isActive() {
        return !DELIVERED.equals(status) && !CANCELLED.equals(status);
    }
}
