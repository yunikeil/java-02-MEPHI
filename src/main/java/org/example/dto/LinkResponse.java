package org.example.dto;

import java.util.UUID;

public class LinkResponse {

    private UUID id;
    private String code;
    private String originalUrl;
    private int clickCount;
    private int maxClicks;
    private boolean expired;

    public LinkResponse(UUID id,
                        String code,
                        String originalUrl,
                        int clickCount,
                        int maxClicks,
                        boolean expired) {
        this.id = id;
        this.code = code;
        this.originalUrl = originalUrl;
        this.clickCount = clickCount;
        this.maxClicks = maxClicks;
        this.expired = expired;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public int getClickCount() {
        return clickCount;
    }

    public int getMaxClicks() {
        return maxClicks;
    }

    public boolean isExpired() {
        return expired;
    }
}
