package com.centralchain.app.models;

// roles from the api
public class Role {
    public static final String ADMIN = "ADMIN";
    public static final String BUYER = "BUYER";
    public static final String SUPPLIER = "SUPPLIER";
    public static final String DRIVER = "DRIVER";

    // name to show
    public static String getLabel(String role) {
        if (BUYER.equals(role)) return "Buyer";
        if (SUPPLIER.equals(role)) return "Supplier";
        if (DRIVER.equals(role)) return "Driver";
        if (ADMIN.equals(role)) return "Admin";
        return "";
    }

    // all roles, used for the admin user form
    public static final String[] ALL = {ADMIN, BUYER, SUPPLIER, DRIVER};
}
