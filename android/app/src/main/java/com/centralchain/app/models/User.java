package com.centralchain.app.models;

public class User {
    public long userId;
    public String fullName;
    public String email;
    public int roleId;
    public String roleName;

    // text for spinner
    @Override
    public String toString() {
        return fullName;
    }
}
