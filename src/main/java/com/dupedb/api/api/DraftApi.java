package com.dupedb.api.api;

import com.dupedb.api.exception.DupeDBException;
import com.dupedb.api.internal.HttpExecutor;
import com.dupedb.api.model.Draft;

import java.util.Map;

/** API client for draft exploit endpoints ({@code /api/exploits/draft}). */
public class DraftApi {
    private final HttpExecutor http;

    public DraftApi(HttpExecutor http) {
        this.http = http;
    }

    /** Gets the current user's draft, or null if none exists. Calls {@code GET /api/exploits/draft}. */
    public Draft getCurrent() throws DupeDBException {
        DraftResponse response = http.get("/api/exploits/draft", DraftResponse.class);
        return response != null ? response.draft() : null;
    }

    /** Creates a new draft. Only one draft per account. Calls {@code POST /api/exploits/draft}. */
    public Draft create(Map<String, Object> data) throws DupeDBException {
        DraftResponse response = http.post("/api/exploits/draft", data, DraftResponse.class);
        return response != null ? response.draft() : null;
    }

    /** Updates a draft. Calls {@code PUT /api/exploits/draft/:id}. */
    public Draft update(String id, Map<String, Object> data) throws DupeDBException {
        DraftResponse response = http.put("/api/exploits/draft/" + id, data, DraftResponse.class);
        return response != null ? response.draft() : null;
    }

    /** Deletes a draft. Calls {@code DELETE /api/exploits/draft/:id}. */
    public void delete(String id) throws DupeDBException {
        http.delete("/api/exploits/draft/" + id);
    }

    /** Submits a draft for review. Calls {@code POST /api/exploits/draft/:id/submit}. */
    public void submit(String id) throws DupeDBException {
        http.post("/api/exploits/draft/" + id + "/submit", Map.of(), Void.class);
    }

    /** Wrapper for the server's nested {@code {"draft": {...}}} response. */
    private record DraftResponse(Draft draft) {}
}
