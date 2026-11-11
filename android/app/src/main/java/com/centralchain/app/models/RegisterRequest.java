package com.centralchain.app.models;

public class RegisterRequest {
    public String fullName;
    public String email;
    public String password;
    public String roleName;

    public RegisterRequest(String fullName, String email, String password, String roleName) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.roleName = roleName;
    }
}
