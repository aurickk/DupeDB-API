package com.dupedb.api.model;

import java.util.List;

/** A plugin referenced by exploits, with known version strings. */
public record Plugin(String name, List<String> versions) {}
