package com.loginApp.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ErrorResponse {
    private int statusCode;
    private String error;
    private String message;
    private String path;
    private String timestamp;
    public ErrorResponse(int statusCode,String error, String message, String path) {
        this.statusCode=statusCode;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now().toString();
    }
}
