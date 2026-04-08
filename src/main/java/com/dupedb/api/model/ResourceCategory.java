package com.dupedb.api.model;

/** A resource category. */
public record ResourceCategory(
    int id,
    String name,
    int displayOrder,
    String createdAt,
    String updatedAt
) {}
