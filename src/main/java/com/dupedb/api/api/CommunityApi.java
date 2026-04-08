package com.dupedb.api.api;

import com.dupedb.api.exception.DupeDBException;
import com.dupedb.api.internal.HttpExecutor;
import com.dupedb.api.model.Community;
import com.dupedb.api.model.CommunityDetection;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** API client for community endpoints ({@code /api/communities}). */
public class CommunityApi {
    private final HttpExecutor http;

    public CommunityApi(HttpExecutor http) {
        this.http = http;
    }

    /** Lists all visible communities. Calls {@code GET /api/communities/}. */
    public List<Community> list() throws DupeDBException {
        record CommunitiesResponse(List<Community> communities) {}
        CommunitiesResponse response = http.get("/api/communities/", CommunitiesResponse.class);
        return response != null ? response.communities() : List.of();
    }

    /** Detects platform and identifier from a URL. Calls {@code GET /api/communities/detect}. */
    public CommunityDetection detect(String url) throws DupeDBException {
        String encoded = URLEncoder.encode(url, StandardCharsets.UTF_8);
        return http.get("/api/communities/detect?url=" + encoded, CommunityDetection.class);
    }

    /** Gets a community by ID. Calls {@code GET /api/communities/:id}. */
    public Community getById(int id) throws DupeDBException {
        record CommunityResponse(Community community) {}
        CommunityResponse response = http.get("/api/communities/" + id, CommunityResponse.class);
        return response != null ? response.community() : null;
    }
}
