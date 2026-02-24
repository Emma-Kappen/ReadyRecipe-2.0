package com.readyrecipe.android.models;

public class SignUpRequest {
    private String email;
    private String password;
    private String username;

    public SignUpRequest(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;
    }

    // Backwards-compatible constructor used by SignUpActivity (no username provided)
    public SignUpRequest(String email, String password) {
        this.email = email;
        this.password = password;
        this.username = "";
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public void setProfile(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;
    }
}
