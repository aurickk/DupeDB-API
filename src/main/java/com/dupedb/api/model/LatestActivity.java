package com.dupedb.api.model;

/** The most recently published or verified exploit, for homepage activity display. */
public record LatestActivity(
    int id,
    String name,
    String status,
    String dateSubmitted,
    String dateModified,
    String author,
    Integer authorUserId,
    String authorDisplayName,
    String authorCustomAvatar,
    String authorDiscordId,
    String authorDiscordAvatar
) {}
