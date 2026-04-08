package com.dupedb.api.api;

import com.dupedb.api.exception.DupeDBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CommentApiTest {

    private RecordingExecutor recorder;
    private CommentApi api;

    @BeforeEach
    void setUp() {
        recorder = new RecordingExecutor();
        api = new CommentApi(recorder);
    }

    // --- list ---

    @Test
    void list_delegatesToGetWithCorrectPath() throws DupeDBException {
        api.list("exploit-123");

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_TYPE", call.method());
        assertEquals("/api/exploits/exploit-123/comments", call.path());
    }

    // --- add ---

    @Test
    void add_delegatesToPostWithCorrectPath() throws DupeDBException {
        api.add("exploit-123", "Great find!");

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("POST", call.method());
        assertEquals("/api/exploits/exploit-123/comments", call.path());
    }

    @Test
    @SuppressWarnings("unchecked")
    void add_passesContentInBody() throws DupeDBException {
        api.add("exploit-123", "Great find!");

        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        Map<String, Object> body = (Map<String, Object>) call.body();
        assertEquals("Great find!", body.get("content"));
    }
}
