package com.dupedb.api.api;

import com.dupedb.api.exception.DupeDBException;
import com.dupedb.api.internal.HttpExecutor;
import com.dupedb.api.model.Comment;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/** API client for exploit comment endpoints ({@code /api/exploits/:id/comments}). */
public class CommentApi {
    private final HttpExecutor http;

    public CommentApi(HttpExecutor http) {
        this.http = http;
    }

    /** Lists comments on an exploit. Calls {@code GET /api/exploits/:id/comments}. */
    public List<Comment> list(String exploitId) throws DupeDBException {
        Type type = new TypeToken<List<Comment>>() {}.getType();
        return http.get("/api/exploits/" + exploitId + "/comments", type);
    }

    /** Adds a comment to an exploit. Calls {@code POST /api/exploits/:id/comments}. */
    public Comment add(String exploitId, String content) throws DupeDBException {
        return http.post("/api/exploits/" + exploitId + "/comments", Map.of("content", content), Comment.class);
    }
}
