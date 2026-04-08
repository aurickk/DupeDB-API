package com.dupedb.api.exception;

/**
 * Thrown when the API returns 429 Too Many Requests.
 * Contains the number of seconds to wait before retrying.
 */
public class RateLimitException extends DupeDBException {
    private final int retryAfterSeconds;

    public RateLimitException(int retryAfterSeconds) {
        super("Rate limited. Retry after " + retryAfterSeconds + " seconds");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /**
     * Returns the number of seconds to wait before retrying.
     */
    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
