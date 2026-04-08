package com.dupedb.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** Site-wide statistics from DupeDB. */
public record SiteStats(
    int total,
    @SerializedName("byType") List<TypeCount> byType,
    @SerializedName("byStatus") List<StatusCount> byStatus,
    @SerializedName("recentCount") int recentCount,
    @SerializedName("verifiedCount") int verifiedCount,
    @SerializedName("unverifiedCount") int unverifiedCount,
    @SerializedName("userCount") int userCount
) {}
