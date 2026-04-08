package com.dupedb.api.auth;

/**
 * Provides authentication tokens for API requests.
 */
public interface TokenProvider {
    /**
     * Returns the current authentication token, or null if not authenticated.
     */
    String getToken();
}
