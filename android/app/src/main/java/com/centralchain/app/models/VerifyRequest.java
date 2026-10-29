package com.centralchain.app.models;

public class VerifyRequest {
    public String email;
    public String verificationCode;

    public VerifyRequest(String email, String verificationCode) {
        this.email = email;
        this.verificationCode = verificationCode;
    }
}
