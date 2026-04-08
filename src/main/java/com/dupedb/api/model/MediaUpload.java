package com.dupedb.api.model;

import com.google.gson.annotations.SerializedName;

/** Result of a media file upload. */
public record MediaUpload(
    boolean success,
    String url,
    String filename,
    long size,
    String type,
    String mimetype,
    @SerializedName("thumbnailUrl") String thumbnailUrl,
    @SerializedName("draftId") String draftId,
    @SerializedName("exploitId") String exploitId,
    @SerializedName("resourceId") Integer resourceId,
    @SerializedName("uploadType") String uploadType
) {}
