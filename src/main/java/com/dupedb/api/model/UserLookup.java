package com.dupedb.api.model;

/** Reduced user profile from the username lookup endpoint. */
public record UserLookup(
    int id,
    String username,
    String displayName,
    String role,
    String customAvatar,
    String discordId,
    String discordAvatar
) {}
