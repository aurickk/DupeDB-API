package com.dupedb.api.api;

import com.dupedb.api.exception.DupeDBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CommunityApi path construction and method delegation.
 */
class CommunityApiTest {

    private RecordingExecutor recorder;
    private CommunityApi api;

    @BeforeEach
    void setUp() {
        recorder = new RecordingExecutor();
        api = new CommunityApi(recorder);
    }

    @Test
    void list_delegatesToGetWithCorrectPath() throws DupeDBException {
        api.list();

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_CLASS", call.method());
        assertEquals("/api/communities/", call.path());
    }

    @Test
    void detect_delegatesToGetWithUrlParam() throws DupeDBException {
        api.detect("https://discord.gg/test");

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_CLASS", call.method());
        assertTrue(call.path().startsWith("/api/communities/detect?url="));
        assertTrue(call.path().contains("discord.gg"));
    }

    @Test
    void detect_encodesSpecialCharacters() throws DupeDBException {
        api.detect("https://youtube.com/@user&test=1");

        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertTrue(call.path().contains("youtube.com"));
        assertFalse(call.path().contains("&test=1"));
    }

    @Test
    void getById_delegatesToGetWithIdInPath() throws DupeDBException {
        api.getById(5);

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_CLASS", call.method());
        assertEquals("/api/communities/5", call.path());
    }
}
