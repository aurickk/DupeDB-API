package com.dupedb.api.model;

import com.google.gson.annotations.SerializedName;

/** Result of deleting an uploaded media file. */
public record DeleteMediaResult(
    boolean success,
    @SerializedName("freedSize") long freedSize,
    String message
) {}
