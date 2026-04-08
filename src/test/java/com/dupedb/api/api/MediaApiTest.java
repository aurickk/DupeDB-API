package com.dupedb.api.api;

import com.dupedb.api.exception.DupeDBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MediaApi path construction and method delegation.
 */
class MediaApiTest {

    private RecordingExecutor recorder;
    private MediaApi api;

    @BeforeEach
    void setUp() {
        recorder = new RecordingExecutor();
        api = new MediaApi(recorder);
    }

    // --- uploadProfilePicture ---

    @Test
    @SuppressWarnings("unchecked")
    void uploadProfilePicture_delegatesToPostMultipartWithAvatarField() throws DupeDBException {
        Path image = Path.of("test-avatar.png");
        api.uploadProfilePicture(image);

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("POST_MULTIPART", call.method());
        assertEquals("/api/auth/profile-picture", call.path());
        Map<String, Object> parts = (Map<String, Object>) call.body();
        assertEquals(image, parts.get("avatar"));
    }

    // --- deleteProfilePicture ---

    @Test
    void deleteProfilePicture_delegatesToDeleteWithCorrectPath() throws DupeDBException {
        api.deleteProfilePicture();

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("DELETE", call.method());
        assertEquals("/api/auth/profile-picture", call.path());
    }

    // --- uploadMedia ---

    @Test
    @SuppressWarnings("unchecked")
    void uploadMedia_withFileOnly_delegatesToPostMultipartWithMediaField() throws DupeDBException {
        Path file = Path.of("screenshot.png");
        api.uploadMedia(file);

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("POST_MULTIPART", call.method());
        assertEquals("/api/upload/media", call.path());
        Map<String, Object> parts = (Map<String, Object>) call.body();
        assertEquals(file, parts.get("media"));
        assertEquals(1, parts.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void uploadMedia_withOptionalFields_includesNonNullFields() throws DupeDBException {
        Path file = Path.of("screenshot.png");
        api.uploadMedia(file, "draft-1", null, "exploit", null);

        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        Map<String, Object> parts = (Map<String, Object>) call.body();
        assertEquals(file, parts.get("media"));
        assertEquals("draft-1", parts.get("draftId"));
        assertEquals("exploit", parts.get("uploadType"));
        assertFalse(parts.containsKey("exploitId"));
        assertFalse(parts.containsKey("resourceId"));
    }

    // --- deleteMedia ---

    @Test
    void deleteMedia_delegatesToDeleteWithResponseAndFilenameInPath() throws DupeDBException {
        api.deleteMedia("screenshot-abc123.png");

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("DELETE_RESPONSE", call.method());
        assertEquals("/api/upload/media/screenshot-abc123.png", call.path());
    }
}
