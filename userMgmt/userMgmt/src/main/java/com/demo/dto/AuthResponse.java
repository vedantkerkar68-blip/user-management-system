package com.demo.dto;

public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private Long id;
    private String employeeId;
    private String email;
    private String fullName;
    private String role;

    public AuthResponse() {}

    public AuthResponse(String token, Long id, String employeeId, String email, String fullName, String role) {
        this.token = token;
        this.id = id;
        this.employeeId = employeeId;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}