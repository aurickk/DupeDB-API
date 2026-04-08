package com.dupedb.api.exception;

/**
 * Thrown when the OAuth2 authentication flow fails.
 */
public class OAuthException extends DupeDBException {
    public OAuthException(String message) {
        super(message);
    }

    public OAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
