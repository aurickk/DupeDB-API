package com.dupedb.api.model;

/** Health check result from {@code /api/health}. */
public record HealthStatus(String status, String timestamp, String error) {}
