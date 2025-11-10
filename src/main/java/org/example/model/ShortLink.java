package org.example.model;

import java.util.UUID;

public class ShortLink {

    private final UUID id;
    private final UUID ownerId;
    private String originalUrl;
    private final String code;
    private final long createdAtMillis;
    private long ttlMillis;
    private int maxClicks;
    private int clickCount;

    public ShortLink(UUID ownerId,
                     String originalUrl,
                     String code,
                     long ttlMillis,
                     int maxClicks) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.originalUrl = originalUrl;
        this.code = code;
        this.createdAtMillis = System.currentTimeMillis();
        this.ttlMillis = ttlMillis;
        this.maxClicks = maxClicks;
        this.clickCount = 0;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getCode() {
        return code;
    }

    public int getClickCount() {
        return clickCount;
    }

    public int getMaxClicks() {
        return maxClicks;
    }

    public void setMaxClicks(int maxClicks) {
        this.maxClicks = maxClicks;
    }

    public long getTtlMillis() {
        return ttlMillis;
    }

    public void setTtlMillis(long ttlMillis) {
        this.ttlMillis = ttlMillis;
    }

    public boolean isExpired() {
        long now = System.currentTimeMillis();
        return now - createdAtMillis > ttlMillis;
    }

    public boolean isLimitReached() {
        return clickCount >= maxClicks;
    }

    public boolean canRedirect() {
        return !isExpired() && !isLimitReached();
    }

    public void incrementClick() {
        this.clickCount++;
    }
}
