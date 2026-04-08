package com.dupedb.api.model;

import com.google.gson.annotations.SerializedName;

/** A resource entry (guide, mod, article, etc.). */
public record Resource(
    int id,
    String title,
    String slug,
    String description,
    String content,
    @SerializedName("categoryId") Integer categoryId,
    @SerializedName("categoryName") String categoryName,
    @SerializedName("resourceType") String resourceType,
    @SerializedName("externalUrl") String externalUrl,
    @SerializedName("iconUrl") String iconUrl,
    @SerializedName("bannerUrl") String bannerUrl,
    @SerializedName("isPublished") boolean isPublished,
    @SerializedName("createdBy") int createdBy,
    @SerializedName("authorUsername") String authorUsername,
    @SerializedName("authorDisplayName") String authorDisplayName,
    @SerializedName("authorDiscordId") String authorDiscordId,
    @SerializedName("authorDiscordAvatar") String authorDiscordAvatar,
    @SerializedName("authorCustomAvatar") String authorCustomAvatar,
    @SerializedName("authorRole") String authorRole,
    @SerializedName("createdAt") String createdAt,
    @SerializedName("updatedAt") String updatedAt
) {}
