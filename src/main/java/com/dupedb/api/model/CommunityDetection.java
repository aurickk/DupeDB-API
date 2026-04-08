package com.dupedb.api.model;

import com.google.gson.annotations.SerializedName;

/** Result of community URL platform detection. Fields are null when {@code detected} is false (except {@code hint}). */
public record CommunityDetection(
    boolean detected,
    String platform,
    @SerializedName("platformId") String platformId,
    @SerializedName("apiConfigured") Boolean apiConfigured,
    @SerializedName("apiRequired") Boolean apiRequired,
    String hint
) {}
