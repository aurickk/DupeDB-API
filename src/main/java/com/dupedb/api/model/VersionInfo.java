package com.dupedb.api.model;

import com.google.gson.annotations.SerializedName;

/** Site version information from {@code /api/version}. */
public record VersionInfo(
    @SerializedName("commitCount") int commitCount,
    @SerializedName("lastCommitMessage") String lastCommitMessage,
    @SerializedName("lastCommitDate") String lastCommitDate,
    @SerializedName("authorName") String authorName
) {}
