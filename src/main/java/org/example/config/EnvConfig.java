package org.example.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {

    @Bean
    public AppSettings appSettings() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        long ttlSeconds = parseLong(dotenv.get("TTL_SECONDS"), 3600L);
        int maxClicks = (int) parseLong(dotenv.get("MAX_CLICKS"), 10L);
        long cleanupSeconds = parseLong(dotenv.get("CLEANUP_INTERVAL_SECONDS"), 30L);
        String baseUrl = dotenv.get("BASE_URL");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:8080";
        }

        return new AppSettings(
                ttlSeconds * 1000L,
                maxClicks,
                cleanupSeconds * 1000L,
                baseUrl
        );
    }

    private long parseLong(String value, long defaultValue) {
        try {
            return value == null ? defaultValue : Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
