package com.dupedb.api.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RateLimitTracker's header parsing and per-endpoint state tracking.
 */
class RateLimitTrackerTest {

    private RateLimitTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new RateLimitTracker();
    }

    @Test
    void updateFromHeaders_parsesAllThreeHeaders() {
        HttpHeaders headers = buildHeaders(Map.of(
            "RateLimit-Limit", List.of("200"),
            "RateLimit-Remaining", List.of("150"),
            "RateLimit-Reset", List.of("900")
        ));

        tracker.updateFromHeaders("/api/exploits/search", headers);

        assertEquals(150, tracker.getRemainingRequests("/api/exploits/search"));
        assertEquals(900, tracker.getResetSeconds("/api/exploits/search"));
    }

    @Test
    void getRemainingRequests_returnsCorrectValueAfterUpdate() {
        HttpHeaders headers = buildHeaders(Map.of(
            "RateLimit-Limit", List.of("30"),
            "RateLimit-Remaining", List.of("5"),
            "RateLimit-Reset", List.of("45")
        ));

        tracker.updateFromHeaders("/api/exploits/search", headers);

        assertEquals(5, tracker.getRemainingRequests("/api/exploits/search"));
    }

    @Test
    void getResetSeconds_returnsCorrectValueAfterUpdate() {
        HttpHeaders headers = buildHeaders(Map.of(
            "RateLimit-Limit", List.of("15"),
            "RateLimit-Remaining", List.of("3"),
            "RateLimit-Reset", List.of("30")
        ));

        tracker.updateFromHeaders("/api/exploits/abc123", headers);

        assertEquals(30, tracker.getResetSeconds("/api/exploits/abc123"));
    }

    @Test
    void isLimited_returnsTrueWhenRemainingIsZero() {
        HttpHeaders headers = buildHeaders(Map.of(
            "RateLimit-Limit", List.of("30"),
            "RateLimit-Remaining", List.of("0"),
            "RateLimit-Reset", List.of("60")
        ));

        tracker.updateFromHeaders("/api/exploits/search", headers);

        assertTrue(tracker.isLimited("/api/exploits/search"));
    }

    @Test
    void isLimited_returnsFalseWhenRemainingGreaterThanZero() {
        HttpHeaders headers = buildHeaders(Map.of(
            "RateLimit-Limit", List.of("30"),
            "RateLimit-Remaining", List.of("15"),
            "RateLimit-Reset", List.of("60")
        ));

        tracker.updateFromHeaders("/api/exploits/search", headers);

        assertFalse(tracker.isLimited("/api/exploits/search"));
    }

    @Test
    void unknownEndpoint_returnsUnlimited() {
        assertEquals(Integer.MAX_VALUE, tracker.getRemainingRequests("/api/unknown/path"));
        assertFalse(tracker.isLimited("/api/unknown/path"));
    }

    @Test
    void unknownEndpoint_resetSecondsReturnsZero() {
        assertEquals(0, tracker.getResetSeconds("/api/unknown/path"));
    }

    @Test
    void missingHeaders_handledGracefully() {
        HttpHeaders emptyHeaders = buildHeaders(Map.of());

        // Should not throw
        tracker.updateFromHeaders("/api/exploits/search", emptyHeaders);

        // Should remain in unknown/unlimited state
        assertEquals(Integer.MAX_VALUE, tracker.getRemainingRequests("/api/exploits/search"));
        assertFalse(tracker.isLimited("/api/exploits/search"));
    }

    @Test
    void partialHeaders_handledGracefully() {
        // Only RateLimit-Limit present, others missing
        HttpHeaders headers = buildHeaders(Map.of(
            "RateLimit-Limit", List.of("200")
        ));

        tracker.updateFromHeaders("/api/exploits/search", headers);

        // Without Remaining header, should not update state
        assertEquals(Integer.MAX_VALUE, tracker.getRemainingRequests("/api/exploits/search"));
    }

    @Test
    void multipleEndpoints_trackedIndependently() {
        HttpHeaders searchHeaders = buildHeaders(Map.of(
            "RateLimit-Limit", List.of("30"),
            "RateLimit-Remaining", List.of("10"),
            "RateLimit-Reset", List.of("45")
        ));
        HttpHeaders voteHeaders = buildHeaders(Map.of(
            "RateLimit-Limit", List.of("60"),
            "RateLimit-Remaining", List.of("0"),
            "RateLimit-Reset", List.of("30")
        ));

        tracker.updateFromHeaders("/api/exploits/search", searchHeaders);
        tracker.updateFromHeaders("/api/exploits/abc/vote", voteHeaders);

        assertEquals(10, tracker.getRemainingRequests("/api/exploits/search"));
        assertFalse(tracker.isLimited("/api/exploits/search"));

        assertEquals(0, tracker.getRemainingRequests("/api/exploits/abc/vote"));
        assertTrue(tracker.isLimited("/api/exploits/abc/vote"));
    }

    @Test
    void updateOverwritesPreviousState() {
        HttpHeaders initial = buildHeaders(Map.of(
            "RateLimit-Limit", List.of("30"),
            "RateLimit-Remaining", List.of("0"),
            "RateLimit-Reset", List.of("60")
        ));
        HttpHeaders refreshed = buildHeaders(Map.of(
            "RateLimit-Limit", List.of("30"),
            "RateLimit-Remaining", List.of("30"),
            "RateLimit-Reset", List.of("60")
        ));

        tracker.updateFromHeaders("/api/exploits/search", initial);
        assertTrue(tracker.isLimited("/api/exploits/search"));

        tracker.updateFromHeaders("/api/exploits/search", refreshed);
        assertFalse(tracker.isLimited("/api/exploits/search"));
        assertEquals(30, tracker.getRemainingRequests("/api/exploits/search"));
    }

    @Test
    void nonNumericHeaders_handledGracefully() {
        HttpHeaders headers = buildHeaders(Map.of(
            "RateLimit-Limit", List.of("not-a-number"),
            "RateLimit-Remaining", List.of("abc"),
            "RateLimit-Reset", List.of("xyz")
        ));

        // Should not throw
        tracker.updateFromHeaders("/api/exploits/search", headers);

        // Should remain in unknown/unlimited state (invalid parse)
        assertEquals(Integer.MAX_VALUE, tracker.getRemainingRequests("/api/exploits/search"));
    }

    // --- Helper methods ---

    private static HttpHeaders buildHeaders(Map<String, List<String>> headerMap) {
        return HttpHeaders.of(headerMap, (name, value) -> true);
    }
}
