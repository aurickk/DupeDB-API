package com.dupedb.api.model;

import com.google.gson.annotations.SerializedName;

/** A connected OAuth application the user has authorized. */
public record ConnectedApp(
    int id,
    @SerializedName("appId") String appId,
    @SerializedName("appName") String appName,
    @SerializedName("readOnly") boolean readOnly,
    @SerializedName("createdAt") String createdAt,
    @SerializedName("lastUsedAt") String lastUsedAt
) {}
