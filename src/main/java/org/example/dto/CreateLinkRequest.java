package org.example.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateLinkRequest {

    @NotBlank
    private String url;

    public CreateLinkRequest() {}

    public CreateLinkRequest(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
