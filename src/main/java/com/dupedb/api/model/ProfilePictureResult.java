package com.dupedb.api.model;

import com.google.gson.annotations.SerializedName;

/** Result of a profile picture upload. */
public record ProfilePictureResult(
    boolean success,
    String message,
    @SerializedName("avatarUrl") String avatarUrl
) {}
