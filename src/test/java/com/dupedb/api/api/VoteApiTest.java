package com.dupedb.api.api;

import com.dupedb.api.exception.DupeDBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VoteApiTest {

    private RecordingExecutor recorder;
    private VoteApi api;

    @BeforeEach
    void setUp() {
        recorder = new RecordingExecutor();
        api = new VoteApi(recorder);
    }

    // --- get ---

    @Test
    void get_delegatesToGetWithCorrectPath() throws DupeDBException {
        api.get("exploit-123");

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_CLASS", call.method());
        assertEquals("/api/exploits/exploit-123/vote", call.path());
    }

    // --- vote ---

    @Test
    void vote_delegatesToPostWithCorrectPath() throws DupeDBException {
        api.vote("exploit-123", "up");

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("POST", call.method());
        assertEquals("/api/exploits/exploit-123/vote", call.path());
    }

    @Test
    @SuppressWarnings("unchecked")
    void vote_passesTypeInBody() throws DupeDBException {
        api.vote("exploit-123", "down");

        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        Map<String, Object> body = (Map<String, Object>) call.body();
        assertEquals("down", body.get("type"));
    }
}
