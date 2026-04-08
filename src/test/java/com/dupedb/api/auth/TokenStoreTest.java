package com.dupedb.api.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TokenStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void credentialsRecordHoldsTokenAppIdCreatedAt() {
        Credentials creds = new Credentials("dupe_abc123", "my-app", "2026-04-06T12:00:00Z");

        assertEquals("dupe_abc123", creds.token());
        assertEquals("my-app", creds.appId());
        assertEquals("2026-04-06T12:00:00Z", creds.createdAt());
    }

    @Test
    void saveWritesJsonToConfiguredPath() throws IOException {
        Path tokenFile = tempDir.resolve("token.json");
        TokenStore store = new TokenStore(tokenFile);

        store.save(new Credentials("dupe_abc123", "my-app", "2026-04-06T12:00:00Z"));

        assertTrue(Files.exists(tokenFile));
        String content = Files.readString(tokenFile);
        assertTrue(content.contains("dupe_abc123"), "Should contain token value");
        assertTrue(content.contains("my-app"), "Should contain appId value");
        assertTrue(content.contains("2026-04-06T12:00:00Z"), "Should contain createdAt value");
    }

    @Test
    void loadReadsJsonFromConfiguredPath() throws IOException {
        Path tokenFile = tempDir.resolve("token.json");
        TokenStore store = new TokenStore(tokenFile);

        store.save(new Credentials("dupe_token_xyz", "scanner-app", "2026-04-06T15:00:00Z"));

        Credentials loaded = store.load();
        assertNotNull(loaded);
        assertEquals("dupe_token_xyz", loaded.token());
        assertEquals("scanner-app", loaded.appId());
        assertEquals("2026-04-06T15:00:00Z", loaded.createdAt());
    }

    @Test
    void loadReturnsNullWhenFileDoesNotExist() {
        Path nonexistent = tempDir.resolve("missing/token.json");
        TokenStore store = new TokenStore(nonexistent);

        Credentials loaded = store.load();
        assertNull(loaded, "Should return null when file does not exist");
    }

    @Test
    void deleteRemovesTheFile() throws IOException {
        Path tokenFile = tempDir.resolve("token.json");
        TokenStore store = new TokenStore(tokenFile);

        store.save(new Credentials("dupe_abc123", "my-app", "2026-04-06T12:00:00Z"));
        assertTrue(Files.exists(tokenFile));

        store.delete();
        assertFalse(Files.exists(tokenFile));
    }

    @Test
    void deleteDoesNotThrowWhenFileDoesNotExist() {
        Path nonexistent = tempDir.resolve("nonexistent.json");
        TokenStore store = new TokenStore(nonexistent);

        assertDoesNotThrow(store::delete);
    }

    @Test
    void saveCreatesParentDirectoriesIfNeeded() throws IOException {
        Path nested = tempDir.resolve("a/b/c/token.json");
        TokenStore store = new TokenStore(nested);

        store.save(new Credentials("dupe_abc123", "my-app", "2026-04-06T12:00:00Z"));

        assertTrue(Files.exists(nested));
        Credentials loaded = store.load();
        assertNotNull(loaded);
        assertEquals("dupe_abc123", loaded.token());
    }

    @Test
    void defaultPathUsesUserHomeDupedbTokenJson() {
        TokenStore store = new TokenStore();
        Path expected = Path.of(System.getProperty("user.home"), ".dupedb", "token.json");

        assertEquals(expected, store.getFilePath());
    }

    @Test
    void customPathOverridesDefault() {
        Path custom = Path.of("/custom/path/token.json");
        TokenStore store = new TokenStore(custom);

        assertEquals(custom, store.getFilePath());
    }
}
