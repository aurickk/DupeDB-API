package com.dupedb.api.api;

import com.dupedb.api.exception.DupeDBException;
import com.dupedb.api.internal.HttpExecutor;
import com.dupedb.api.model.DeleteMediaResult;
import com.dupedb.api.model.MediaUpload;
import com.dupedb.api.model.ProfilePictureResult;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** API client for file uploads and profile pictures ({@code /api/upload}, {@code /api/auth/profile-picture}). */
public class MediaApi {
    private final HttpExecutor http;

    public MediaApi(HttpExecutor http) {
        this.http = http;
    }

    /** Uploads a new profile picture. Calls {@code POST /api/auth/profile-picture}. */
    public ProfilePictureResult uploadProfilePicture(Path imageFile) throws DupeDBException {
        Map<String, Object> parts = Map.of("avatar", imageFile);
        return http.postMultipart("/api/auth/profile-picture", parts, ProfilePictureResult.class);
    }

    /** Removes the current user's profile picture. Calls {@code DELETE /api/auth/profile-picture}. */
    public void deleteProfilePicture() throws DupeDBException {
        http.delete("/api/auth/profile-picture");
    }

    /** Uploads a media file without attaching it. Calls {@code POST /api/upload/media}. */
    public MediaUpload uploadMedia(Path file) throws DupeDBException {
        return uploadMedia(file, null, null, null, null);
    }

    /** Uploads a media file with optional attachment to a draft, exploit, or resource. Calls {@code POST /api/upload/media}. */
    public MediaUpload uploadMedia(Path file, String draftId, String exploitId,
                                   String uploadType, String resourceId) throws DupeDBException {
        Map<String, Object> parts = new LinkedHashMap<>();
        parts.put("media", file);
        if (draftId != null) parts.put("draftId", draftId);
        if (exploitId != null) parts.put("exploitId", exploitId);
        if (uploadType != null) parts.put("uploadType", uploadType);
        if (resourceId != null) parts.put("resourceId", resourceId);
        return http.postMultipart("/api/upload/media", parts, MediaUpload.class);
    }

    /** Deletes an uploaded media file. Calls {@code DELETE /api/upload/media/:filename}. */
    public DeleteMediaResult deleteMedia(String filename) throws DupeDBException {
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        return http.deleteWithResponse("/api/upload/media/" + encoded, DeleteMediaResult.class);
    }
}
