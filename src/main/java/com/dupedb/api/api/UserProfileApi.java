package com.dupedb.api.api;

import com.dupedb.api.exception.DupeDBException;
import com.dupedb.api.internal.HttpExecutor;
import com.dupedb.api.model.UserLookup;
import com.dupedb.api.model.UserProfile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * API client for viewing other users' profiles ({@code /api/users}).
 * Separate from {@link UserApi} which manages the current user's own profile.
 */
public class UserProfileApi {
    private final HttpExecutor http;

    public UserProfileApi(HttpExecutor http) {
        this.http = http;
    }

    /** Gets a user's public profile. Calls {@code GET /api/users/:id/profile}. */
    public UserProfile getProfile(int userId) throws DupeDBException {
        return http.get("/api/users/" + userId + "/profile", UserProfile.class);
    }

    /** Looks up a user by username or display name (case-insensitive). Calls {@code GET /api/users/lookup/:name}. */
    public UserLookup lookup(String name) throws DupeDBException {
        String encoded = URLEncoder.encode(name, StandardCharsets.UTF_8);
        return http.get("/api/users/lookup/" + encoded, UserLookup.class);
    }
}
