package com.academy.project.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private boolean success;
    private String message;
    private Instant timestamp;
    private Map<String, String> fieldErrors;

    public ErrorResponse(String message) {
        this.success = false;
        this.message = message;
        this.timestamp = Instant.now();
        this.fieldErrors = null;
    }

    public ErrorResponse(String message, Map<String, String> fieldErrors) {
        this.success = false;
        this.message = message;
        this.timestamp = Instant.now();
        this.fieldErrors = fieldErrors;
    }
}
