package com.dupedb.api.api;

import com.dupedb.api.exception.DupeDBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceApiTest {

    private RecordingExecutor recorder;
    private ResourceApi api;

    @BeforeEach
    void setUp() {
        recorder = new RecordingExecutor();
        api = new ResourceApi(recorder);
    }

    @Test
    void categories_delegatesToGetWithCorrectPath() throws DupeDBException {
        api.categories();
        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_CLASS", call.method());
        assertEquals("/api/resources/categories", call.path());
    }

    @Test
    void list_delegatesToGetWithCorrectPath() throws DupeDBException {
        api.list();
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_CLASS", call.method());
        assertEquals("/api/resources/", call.path());
    }

    @Test
    void listByCategory_includesCategoryParam() throws DupeDBException {
        api.list(3);
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("/api/resources/?category=3", call.path());
    }

    @Test
    void getById_delegatesToGetWithIdInPath() throws DupeDBException {
        api.getById(42);
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_CLASS", call.method());
        assertEquals("/api/resources/id/42", call.path());
    }

    @Test
    void getBySlug_delegatesToGetWithSlugInPath() throws DupeDBException {
        api.getBySlug("getting-started");
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_CLASS", call.method());
        assertEquals("/api/resources/getting-started", call.path());
    }
}
