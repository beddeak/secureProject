package com.securearchive.archive.common.error;

public record ApiErrorResponse(
    int status,
    String error,
    String message,
    String path
) {
}
