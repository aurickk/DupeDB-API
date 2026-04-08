package com.dupedb.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** Paginated list of server IP addresses from exploits. */
public record ServerIpResult(
    @SerializedName("serverIps") List<String> serverIps,
    Pagination pagination
) {}
