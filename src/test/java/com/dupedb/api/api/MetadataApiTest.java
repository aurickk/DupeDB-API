package com.dupedb.api.api;

import com.dupedb.api.exception.DupeDBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MetadataApi path construction and method delegation.
 */
class MetadataApiTest {

    private RecordingExecutor recorder;
    private MetadataApi api;

    @BeforeEach
    void setUp() {
        recorder = new RecordingExecutor();
        api = new MetadataApi(recorder);
    }

    @Test
    void health_delegatesToGetWithCorrectPath() throws DupeDBException {
        api.health();

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_CLASS", call.method());
        assertEquals("/api/health", call.path());
    }

    @Test
    void version_delegatesToGetWithCorrectPath() throws DupeDBException {
        api.version();

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_CLASS", call.method());
        assertEquals("/api/version", call.path());
    }

    @Test
    void tags_delegatesToGetWithCorrectPath() throws DupeDBException {
        api.tags();

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_TYPE", call.method());
        assertEquals("/api/tags", call.path());
    }

    @Test
    void versions_delegatesToGetWithCorrectPath() throws DupeDBException {
        api.versions();

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_CLASS", call.method());
        assertEquals("/api/versions", call.path());
    }

    @Test
    void types_delegatesToGetWithCorrectPath() throws DupeDBException {
        api.types();

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_TYPE", call.method());
        assertEquals("/api/types", call.path());
    }

    @Test
    void serverTypes_delegatesToGetWithCorrectPath() throws DupeDBException {
        api.serverTypes();

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_TYPE", call.method());
        assertEquals("/api/server-types", call.path());
    }

    @Test
    void stats_delegatesToGetWithCorrectPath() throws DupeDBException {
        api.stats();

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_CLASS", call.method());
        assertEquals("/api/stats", call.path());
    }

    @Test
    void latestActivity_delegatesToGetWithCorrectPath() throws DupeDBException {
        api.latestActivity();

        assertEquals(1, recorder.getCalls().size());
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_CLASS", call.method());
        assertEquals("/api/latest-activity", call.path());
    }

    @Test
    void serverIps_delegatesToGetWithPaginationParams() throws DupeDBException {
        api.serverIps(2, 100);
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_CLASS", call.method());
        assertEquals("/api/server-ips?page=2&limit=100", call.path());
    }

    @Test
    void serverIps_defaultsToPage1Limit50() throws DupeDBException {
        api.serverIps();
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("/api/server-ips?page=1&limit=50", call.path());
    }

    @Test
    void plugins_delegatesToGetWithCorrectPath() throws DupeDBException {
        api.plugins();
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_TYPE", call.method());
        assertEquals("/api/plugins", call.path());
    }

    @Test
    void publicStats_delegatesToGetWithCorrectPath() throws DupeDBException {
        api.publicStats();
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_CLASS", call.method());
        assertEquals("/api/public/stats", call.path());
    }

    @Test
    void publicStatsHistory_delegatesToGetWithDaysParam() throws DupeDBException {
        api.publicStatsHistory(7);
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_TYPE", call.method());
        assertEquals("/api/public/stats/history?days=7", call.path());
    }

    @Test
    void publicStatsHistory_defaultsTo30Days() throws DupeDBException {
        api.publicStatsHistory();
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("/api/public/stats/history?days=30", call.path());
    }

    @Test
    void siteVisibility_delegatesToGetWithCorrectPath() throws DupeDBException {
        api.siteVisibility();
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_CLASS", call.method());
        assertEquals("/api/site-visibility", call.path());
    }

    @Test
    void publicExploits_delegatesToGetWithCorrectPath() throws DupeDBException {
        api.publicExploits();
        RecordingExecutor.Call call = recorder.getCalls().getFirst();
        assertEquals("GET_TYPE", call.method());
        assertEquals("/api/public/exploits", call.path());
    }
}
