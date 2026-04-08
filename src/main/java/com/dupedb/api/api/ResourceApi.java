package com.dupedb.api.api;

import com.dupedb.api.exception.DupeDBException;
import com.dupedb.api.internal.HttpExecutor;
import com.dupedb.api.model.Resource;
import com.dupedb.api.model.ResourceCategory;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** API client for resource endpoints ({@code /api/resources}). */
public class ResourceApi {
    private final HttpExecutor http;

    public ResourceApi(HttpExecutor http) {
        this.http = http;
    }

    /** Lists all resource categories. Calls {@code GET /api/resources/categories}. */
    public List<ResourceCategory> categories() throws DupeDBException {
        record CategoriesResponse(List<ResourceCategory> categories) {}
        CategoriesResponse response = http.get("/api/resources/categories", CategoriesResponse.class);
        return response != null ? response.categories() : List.of();
    }

    /** Lists all published resources. Calls {@code GET /api/resources/}. */
    public List<Resource> list() throws DupeDBException {
        record ResourcesResponse(List<Resource> resources) {}
        ResourcesResponse response = http.get("/api/resources/", ResourcesResponse.class);
        return response != null ? response.resources() : List.of();
    }

    /** Lists published resources filtered by category. Calls {@code GET /api/resources/?category=:categoryId}. */
    public List<Resource> list(int categoryId) throws DupeDBException {
        record ResourcesResponse(List<Resource> resources) {}
        ResourcesResponse response = http.get("/api/resources/?category=" + categoryId, ResourcesResponse.class);
        return response != null ? response.resources() : List.of();
    }

    /** Gets a resource by ID. Calls {@code GET /api/resources/id/:id}. */
    public Resource getById(int id) throws DupeDBException {
        record ResourceResponse(Resource resource) {}
        ResourceResponse response = http.get("/api/resources/id/" + id, ResourceResponse.class);
        return response != null ? response.resource() : null;
    }

    /** Gets a resource by URL slug. Calls {@code GET /api/resources/:slug}. */
    public Resource getBySlug(String slug) throws DupeDBException {
        String encoded = URLEncoder.encode(slug, StandardCharsets.UTF_8);
        record ResourceResponse(Resource resource) {}
        ResourceResponse response = http.get("/api/resources/" + encoded, ResourceResponse.class);
        return response != null ? response.resource() : null;
    }
}
