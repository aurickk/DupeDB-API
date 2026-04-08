package com.dupedb.api.model;

/** Pagination metadata for paginated API responses. */
public record Pagination(
    int page,
    int pages,
    int total,
    boolean hasMore
) {}
