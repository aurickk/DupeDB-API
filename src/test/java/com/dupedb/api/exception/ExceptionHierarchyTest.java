package com.dupedb.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionHierarchyTest {

    @Test
    void dupeDBExceptionIsCheckedException() {
        assertTrue(Exception.class.isAssignableFrom(DupeDBException.class));
        assertFalse(RuntimeException.class.isAssignableFrom(DupeDBException.class));
    }

    @Test
    void dupeDBExceptionMessage() {
        var ex = new DupeDBException("test error");
        assertEquals("test error", ex.getMessage());
    }

    @Test
    void dupeDBExceptionMessageAndCause() {
        var cause = new RuntimeException("root cause");
        var ex = new DupeDBException("wrapped", cause);
        assertEquals("wrapped", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void authExceptionExtendsDupeDBException() {
        assertTrue(DupeDBException.class.isAssignableFrom(AuthException.class));
        var ex = new AuthException("unauthorized");
        assertEquals("unauthorized", ex.getMessage());
    }

    @Test
    void rateLimitExceptionExtendsDupeDBException() {
        assertTrue(DupeDBException.class.isAssignableFrom(RateLimitException.class));
    }

    @Test
    void rateLimitExceptionRetryAfterSeconds() {
        var ex = new RateLimitException(30);
        assertEquals(30, ex.getRetryAfterSeconds());
        assertTrue(ex.getMessage().contains("30"));
    }

    @Test
    void networkExceptionExtendsDupeDBException() {
        assertTrue(DupeDBException.class.isAssignableFrom(NetworkException.class));
        var cause = new java.io.IOException("connection reset");
        var ex = new NetworkException("network failure", cause);
        assertEquals("network failure", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void apiExceptionExtendsDupeDBException() {
        assertTrue(DupeDBException.class.isAssignableFrom(ApiException.class));
    }

    @Test
    void apiExceptionStatusCode() {
        var ex = new ApiException(404, "not found");
        assertEquals(404, ex.getStatusCode());
        assertEquals("not found", ex.getMessage());
    }

    @Test
    void oauthExceptionExtendsDupeDBException() {
        assertTrue(DupeDBException.class.isAssignableFrom(OAuthException.class));
        var ex = new OAuthException("oauth failed");
        assertEquals("oauth failed", ex.getMessage());
    }

    @Test
    void oauthExceptionWithCause() {
        var cause = new RuntimeException("token expired");
        var ex = new OAuthException("oauth error", cause);
        assertEquals("oauth error", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void allExceptionsAreCatchableAsDupeDBException() {
        // Verify polymorphic catch block works
        DupeDBException[] exceptions = {
            new AuthException("auth"),
            new RateLimitException(10),
            new NetworkException("net", new Exception()),
            new ApiException(500, "server error"),
            new OAuthException("oauth")
        };
        for (DupeDBException ex : exceptions) {
            assertInstanceOf(DupeDBException.class, ex);
        }
    }
}
