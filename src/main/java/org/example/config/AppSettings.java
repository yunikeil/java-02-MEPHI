package org.example.config;

public class AppSettings {
    private long ttlMillis;
    private int maxClicks;
    private long cleanupIntervalMillis;
    private String baseUrl;

    public AppSettings(long ttlMillis, int maxClicks, long cleanupIntervalMillis, String baseUrl) {
        this.ttlMillis = ttlMillis;
        this.maxClicks = maxClicks;
        this.cleanupIntervalMillis = cleanupIntervalMillis;
        this.baseUrl = baseUrl;
    }

    public long getTtlMillis() {
        return ttlMillis;
    }

    public void setTtlMillis(long ttlMillis) {
        this.ttlMillis = ttlMillis;
    }

    public int getMaxClicks() {
        return maxClicks;
    }

    public void setMaxClicks(int maxClicks) {
        this.maxClicks = maxClicks;
    }

    public long getCleanupIntervalMillis() {
        return cleanupIntervalMillis;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
