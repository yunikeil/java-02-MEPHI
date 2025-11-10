package org.example.scheduler;

import org.example.config.AppSettings;
import org.example.service.UrlShortenerService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class CleanupScheduler {

    private final UrlShortenerService service;
    private final AppSettings settings;

    public CleanupScheduler(UrlShortenerService service, AppSettings settings) {
        this.service = service;
        this.settings = settings;
    }

    @Scheduled(fixedDelayString = "#{@appSettings.cleanupIntervalMillis}")
    public void clean() {
        service.cleanupExpired();
    }
}
