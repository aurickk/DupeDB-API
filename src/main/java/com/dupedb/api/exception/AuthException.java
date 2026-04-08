package com.dupedb.api.exception;

/**
 * Thrown when an API request fails due to authentication or authorization issues (401/403).
 */
public class AuthException extends DupeDBException {
    public AuthException(String message) {
        super(message);
    }
}
