package com.centralchain.app.models;

// for admin adding or editing a user
// password can be empty when editing
public class UserRequest {
    public String fullName;
    public String email;
    public String password;
    public String roleName;

    public UserRequest(String fullName, String email, String password, String roleName) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.roleName = roleName;
    }
}
