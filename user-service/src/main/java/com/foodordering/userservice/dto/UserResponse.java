package com.foodordering.userservice.dto;

import com.foodordering.userservice.model.Role;

public class UserResponse {
    private Long customerId;
    private String name;
    private String email;
    private String phone;
    private Role role;

    public UserResponse() {}

    public UserResponse(Long customerId, String name, String email, String phone, Role role) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
