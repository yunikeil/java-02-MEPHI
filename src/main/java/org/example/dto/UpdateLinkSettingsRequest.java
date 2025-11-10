package org.example.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class UpdateLinkSettingsRequest {

    @Min(1)
    @Max(1_000_000)
    private Integer maxClicks;

    @Min(1)
    @Max(31_536_000) // год
    private Long ttlSeconds;

    public UpdateLinkSettingsRequest() {}

    public Integer getMaxClicks() {
        return maxClicks;
    }

    public void setMaxClicks(Integer maxClicks) {
        this.maxClicks = maxClicks;
    }

    public Long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(Long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }
}
