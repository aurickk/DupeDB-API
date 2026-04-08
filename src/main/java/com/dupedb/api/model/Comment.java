package com.dupedb.api.model;

/** A comment on an exploit. */
public record Comment(
    int id,
    String exploitId,
    String author,
    int authorUserId,
    String content,
    String datePosted,
    String authorDisplayName,
    String discordId,
    String discordAvatar,
    String customAvatar,
    String authorRole
) {}
