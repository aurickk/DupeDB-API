package com.dupedb.api.model;

import com.google.gson.annotations.SerializedName;

/** Response after voting on an exploit. */
public record VoteResult(
    int upvotes,
    int downvotes,
    @SerializedName("userVote") Integer userVote
) {}
