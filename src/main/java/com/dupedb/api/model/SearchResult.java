package com.dupedb.api.model;

import java.util.List;

/** Paginated search result wrapping a list of exploits. */
public record SearchResult<T>(
    List<T> exploits,
    Pagination pagination
) {}
