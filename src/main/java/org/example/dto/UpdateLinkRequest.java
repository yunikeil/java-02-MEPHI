package org.example.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateLinkRequest {

    @NotBlank
    private String url;

    public UpdateLinkRequest() {}

    public UpdateLinkRequest(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
