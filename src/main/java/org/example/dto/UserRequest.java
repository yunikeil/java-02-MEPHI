package org.example.dto;

import jakarta.validation.constraints.NotBlank;

public class UserRequest {

    @NotBlank
    private String name;

    public UserRequest() {}

    public UserRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
