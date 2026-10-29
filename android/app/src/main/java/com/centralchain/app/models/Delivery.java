package com.centralchain.app.models;

// a delivery for an order, made when the order is dispatched
public class Delivery {
    // delivery status values
    public static final String ASSIGNED = "ASSIGNED";
    public static final String IN_TRANSIT = "IN_TRANSIT";
    public static final String DELIVERED = "DELIVERED";
    public static final String FAILED = "FAILED";

    public long deliveryId;
    public long poId;
    public String poNumber;
    public int poQuantity;
    public String deliveryAddress;
    public long itemId;
    public String itemSku;
    public String itemName;
    public long driverId;
    public String driverName;
    public String deliveryStatus;
    public String signatureUrl;
    public String photoProofUrl;
    public String deliveredAt;

    // still needs to be delivered
    public boolean isOpen() {
        return ASSIGNED.equals(deliveryStatus) || IN_TRANSIT.equals(deliveryStatus);
    }
}
