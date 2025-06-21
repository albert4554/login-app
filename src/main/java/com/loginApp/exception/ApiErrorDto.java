package com.loginApp.exception;

public record ApiErrorDto(
        String message,
        String backendMessage,
        String method,
        String url
) {
}
