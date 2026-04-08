package com.dupedb.api.internal;

import java.net.http.HttpHeaders;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks rate limit state per endpoint based on HTTP response headers.
 *
 * <p>The DupeDB server uses {@code express-rate-limit} with {@code standardHeaders: true},
 * which sends these headers on every response:
 * <ul>
 *   <li>{@code RateLimit-Limit} -- max requests per window</li>
 *   <li>{@code RateLimit-Remaining} -- remaining requests in current window</li>
 *   <li>{@code RateLimit-Reset} -- seconds until window resets</li>
 * </ul>
 *
 * <p>This tracker stores the most recent rate limit state for each endpoint path,
 * allowing callers to check whether an endpoint is currently rate-limited before
 * making a request.
 *
 * <p>Thread-safe via {@link ConcurrentHashMap}.
 */
public class RateLimitTracker {

    private record LimitState(int limit, int remaining, int resetSeconds, long updatedAt) {}

    private final Map<String, LimitState> limits = new ConcurrentHashMap<>();

    /**
     * Updates the rate limit state for the given endpoint based on response headers.
     *
     * <p>Parses {@code RateLimit-Limit}, {@code RateLimit-Remaining}, and {@code RateLimit-Reset}
     * from the response headers. If any required header is missing or not a valid integer,
     * the state is not updated (treated as unlimited).
     *
     * @param endpoint the API endpoint path (e.g. "/api/exploits/search")
     * @param headers  the HTTP response headers to parse
     */
    public void updateFromHeaders(String endpoint, HttpHeaders headers) {
        OptionalInt limit = parseIntHeader(headers, "RateLimit-Limit");
        OptionalInt remaining = parseIntHeader(headers, "RateLimit-Remaining");
        OptionalInt reset = parseIntHeader(headers, "RateLimit-Reset");

        // Only update state if we have both limit and remaining values
        if (limit.isPresent() && remaining.isPresent()) {
            limits.put(endpoint, new LimitState(
                limit.getAsInt(),
                remaining.getAsInt(),
                reset.orElse(0),
                System.currentTimeMillis()
            ));
        }
    }

    /**
     * Returns the number of remaining requests for the given endpoint.
     * Returns {@link Integer#MAX_VALUE} if the endpoint has no tracked rate limit state.
     *
     * @param endpoint the API endpoint path
     * @return remaining request count, or {@code Integer.MAX_VALUE} if unknown
     */
    public int getRemainingRequests(String endpoint) {
        LimitState state = limits.get(endpoint);
        return state != null ? state.remaining() : Integer.MAX_VALUE;
    }

    /**
     * Returns the number of seconds until the rate limit window resets for the given endpoint.
     * Returns 0 if the endpoint has no tracked rate limit state.
     *
     * @param endpoint the API endpoint path
     * @return reset seconds, or 0 if unknown
     */
    public int getResetSeconds(String endpoint) {
        LimitState state = limits.get(endpoint);
        return state != null ? state.resetSeconds() : 0;
    }

    /**
     * Returns whether the given endpoint is currently rate-limited (remaining requests is 0).
     * Returns {@code false} if the endpoint has no tracked rate limit state (assumed unlimited).
     *
     * @param endpoint the API endpoint path
     * @return {@code true} if the endpoint has exhausted its rate limit window
     */
    public boolean isLimited(String endpoint) {
        LimitState state = limits.get(endpoint);
        return state != null && state.remaining() <= 0;
    }

    /**
     * Parses an integer value from a named HTTP header.
     *
     * @param headers    the response headers
     * @param headerName the header to parse
     * @return the parsed integer, or empty if the header is missing or not a valid integer
     */
    private OptionalInt parseIntHeader(HttpHeaders headers, String headerName) {
        return headers.firstValue(headerName)
            .map(value -> {
                try {
                    return OptionalInt.of(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    return OptionalInt.empty();
                }
            })
            .orElse(OptionalInt.empty());
    }
}
