package com.centralchain.app.models;

// login response from api
// note: it has no user id, we get that after login
public class LoginResponse {
    public String token;
    public String tokenType;
    public String email;
    public String fullName;
    public String roleName;
    public String otp;
}
