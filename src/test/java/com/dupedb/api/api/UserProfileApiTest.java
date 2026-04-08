package com.dupedb.api.api;

import com.dupedb.api.exception.DupeDBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for UserProfileApi path construction and method delegation.
 */
class UserProfileApiTest {

    private RecordingExecutor recorder;
    private UserProfileApi api;

    @BeforeEach
    void setUp() {
        recorder = new RecordingExecutor();
        api = new UserProfileApi(recorder);
    }

    @Test
    void getProfile_delegatesToGetWithUserIdInPath() throws DupeDBException {
        api.getProfile(42);

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_CLASS", call.method());
        assertEquals("/api/users/42/profile", call.path());
    }

    @Test
    void lookup_delegatesToGetWithNameInPath() throws DupeDBException {
        api.lookup("testuser");

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_CLASS", call.method());
        assertEquals("/api/users/lookup/testuser", call.path());
    }

    @Test
    void lookup_urlEncodesNameWithSpaces() throws DupeDBException {
        api.lookup("user name");

        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("/api/users/lookup/user+name", call.path());
    }
}
