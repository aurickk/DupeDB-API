package com.dupedb.api.model;

import java.util.List;

/** Draft exploit record (not yet submitted). */
public record Draft(
    String id,
    String name,
    String description,
    String type,
    String status,
    String edition,
    String platform,
    List<String> minecraftVersions,
    List<String> serverIps,
    List<String> sources,
    List<String> plugins,
    String pluginName,
    String pluginVersion,
    boolean isPluginSpecific,
    String multiplayerType,
    List<String> serverSoftware,
    List<String> modLinks,
    List<String> embeddedVideos,
    String thumbnail,
    List<String> tags,
    boolean isDraft
) {}
