package com.dupedb.api.exception;

/**
 * Thrown when the API returns a non-success HTTP status code (400, 404, 500, etc.).
 */
public class ApiException extends DupeDBException {
    private final int statusCode;

    public ApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Returns the HTTP status code from the API response.
     */
    public int getStatusCode() {
        return statusCode;
    }
}
