package com.hosea;

/** An expected error (bad input, not found, ...) with the HTTP status to send back. */
public class ApiException extends RuntimeException {

    public final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }
}
