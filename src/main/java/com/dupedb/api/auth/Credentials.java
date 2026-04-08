package com.dupedb.api.auth;

/**
 * Holds authentication credentials for the DupeDB API.
 * Persisted to disk by {@link TokenStore} as JSON.
 *
 * @param token     the API token (e.g., {@code dupe_abc123...})
 * @param appId     the OAuth app identifier that issued this token
 * @param createdAt ISO-8601 timestamp of when the token was created
 */
public record Credentials(
    String token,
    String appId,
    String createdAt
) {}
