package org.example.exception;

import java.time.Instant;

public class ApiError {
    private final String message;
    private final String details;
    private final Instant timestamp = Instant.now();

    public ApiError(String message, String details) {
        this.message = message;
        this.details = details;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
