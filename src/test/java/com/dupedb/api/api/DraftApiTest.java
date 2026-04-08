package com.dupedb.api.api;

import com.dupedb.api.exception.DupeDBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DraftApi path construction and method delegation.
 * Verifies all 5 draft endpoints route to the correct paths with correct
 * HTTP methods and that getCurrent() makes exactly one HTTP call.
 */
class DraftApiTest {

    private RecordingExecutor recorder;
    private DraftApi api;

    @BeforeEach
    void setUp() {
        recorder = new RecordingExecutor();
        api = new DraftApi(recorder);
    }

    // --- getCurrent ---

    @Test
    void getCurrent_delegatesToGetWithCorrectPath() throws DupeDBException {
        api.getCurrent();

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_CLASS", call.method());
        assertEquals("/api/exploits/draft", call.path());
    }

    @Test
    void getCurrent_makesExactlyOneHttpCall() throws DupeDBException {
        api.getCurrent();

        // Critical: must be exactly 1 call, not 2 (no dead double-call bug)
        assertEquals(1, recorder.getCalls().size());
    }

    // --- create ---

    @Test
    void create_delegatesToPostWithCorrectPath() throws DupeDBException {
        Map<String, Object> data = Map.of("name", "New Exploit");
        api.create(data);

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("POST", call.method());
        assertEquals("/api/exploits/draft", call.path());
        assertSame(data, call.body());
    }

    // --- update ---

    @Test
    void update_includesDraftIdInPath() throws DupeDBException {
        Map<String, Object> data = Map.of("name", "Updated");
        api.update("draft-456", data);

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("PUT", call.method());
        assertEquals("/api/exploits/draft/draft-456", call.path());
        assertSame(data, call.body());
    }

    // --- delete ---

    @Test
    void delete_callsDeleteWithCorrectPath() throws DupeDBException {
        api.delete("draft-789");

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("DELETE", call.method());
        assertEquals("/api/exploits/draft/draft-789", call.path());
    }

    // --- submit ---

    @Test
    void submit_callsPostWithSubmitSuffix() throws DupeDBException {
        api.submit("draft-abc");

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("POST", call.method());
        assertEquals("/api/exploits/draft/draft-abc/submit", call.path());
    }
}
