package com.dupedb.api.model;

/** Current user's profile from {@code /api/auth/me}. */
public record User(
    int id,
    String username,
    String displayName,
    String discordId,
    String discordUsername,
    String discordAvatar,
    String customAvatar,
    String role,
    String createdAt,
    boolean hideDiscordProfile
) {}
