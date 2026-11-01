package com.wearminutes.service.dto.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ErrorResponse {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now().format(FORMATTER);
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }
}
