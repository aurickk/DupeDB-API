package com.dupedb.api.model;

import com.google.gson.annotations.SerializedName;

/** Current user's vote state for an exploit. */
public record Vote(
    @SerializedName("userVote") int userVote
) {}
