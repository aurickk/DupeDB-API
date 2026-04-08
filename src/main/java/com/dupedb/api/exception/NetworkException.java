package com.dupedb.api.exception;

/**
 * Thrown when an API request fails due to network issues (connection refused, timeout, DNS failure).
 */
public class NetworkException extends DupeDBException {
    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
