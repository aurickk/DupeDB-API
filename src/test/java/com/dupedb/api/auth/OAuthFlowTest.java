package com.dupedb.api.auth;

import com.dupedb.api.exception.AuthException;
import com.dupedb.api.exception.DupeDBException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class OAuthFlowTest {

    @TempDir
    Path tempDir;

    // --- OAuthFlow.parseParam tests ---

    @Test
    void parseParamExtractsCodeFromQuery() {
        String token = OAuthFlow.parseParam("code=dupe_abc123&state=xyz", "code");
        assertEquals("dupe_abc123", token);
    }

    @Test
    void parseParamExtractsErrorFromQuery() {
        String error = OAuthFlow.parseParam("error=access_denied", "error");
        assertEquals("access_denied", error);
    }

    @Test
    void parseParamReturnsNullForMissingParam() {
        String result = OAuthFlow.parseParam("code=abc123", "error");
        assertNull(result);
    }

    @Test
    void parseParamReturnsNullForNullQuery() {
        String result = OAuthFlow.parseParam(null, "code");
        assertNull(result);
    }

    @Test
    void parseParamDecodesUrlEncodedValues() {
        String result = OAuthFlow.parseParam("code=dupe_abc%2B123", "code");
        assertEquals("dupe_abc+123", result);
    }

    @Test
    void parseParamHandlesEmptyValue() {
        String result = OAuthFlow.parseParam("code=&error=denied", "code");
        assertEquals("", result);
    }

    @Test
    void parseParamHandlesValueWithEquals() {
        // value contains = sign
        String result = OAuthFlow.parseParam("code=abc=def", "code");
        assertEquals("abc=def", result);
    }

    // --- OAuthFlow constructor tests ---

    @Test
    void defaultPortIs9876() {
        OAuthFlow flow = new OAuthFlow("https://dupedb.net", "my-app");
        assertEquals(9876, flow.getCallbackPort());
    }

    @Test
    void customPortIsRespected() {
        OAuthFlow flow = new OAuthFlow("https://dupedb.net", "my-app", 8080);
        assertEquals(8080, flow.getCallbackPort());
    }

    @Test
    void appIdIsStored() {
        OAuthFlow flow = new OAuthFlow("https://dupedb.net", "scanner-app");
        assertEquals("scanner-app", flow.getAppId());
    }

    // --- AuthManager tests ---

    @Test
    void directTokenReturnsImmediately() throws DupeDBException {
        AuthManager manager = new AuthManager("dupe_direct_token");
        assertEquals("dupe_direct_token", manager.getToken());
    }

    @Test
    void directTokenHasTokenReturnsTrue() {
        AuthManager manager = new AuthManager("dupe_direct_token");
        assertTrue(manager.hasToken());
    }

    @Test
    void clearTokenRemovesDirectToken() {
        AuthManager manager = new AuthManager("dupe_direct_token");
        assertTrue(manager.hasToken());

        manager.clearToken();
        assertFalse(manager.hasToken());
    }

    @Test
    void getTokenLoadsFromDiskWhenNoMemoryToken() throws DupeDBException, IOException {
        Path tokenFile = tempDir.resolve("token.json");
        TokenStore store = new TokenStore(tokenFile);
        store.save(new Credentials("dupe_disk_token", "my-app", "2026-04-06T12:00:00Z"));

        AuthManager manager = new AuthManager(store, null);
        assertEquals("dupe_disk_token", manager.getToken());
    }

    @Test
    void hasTokenReturnsTrueWhenDiskTokenExists() throws IOException {
        Path tokenFile = tempDir.resolve("token.json");
        TokenStore store = new TokenStore(tokenFile);
        store.save(new Credentials("dupe_disk_token", "my-app", "2026-04-06T12:00:00Z"));

        AuthManager manager = new AuthManager(store, null);
        assertTrue(manager.hasToken());
    }

    @Test
    void hasTokenReturnsFalseWithNoTokenAnywhere() {
        Path tokenFile = tempDir.resolve("nonexistent.json");
        TokenStore store = new TokenStore(tokenFile);

        AuthManager manager = new AuthManager(store, null);
        assertFalse(manager.hasToken());
    }

    @Test
    void getTokenThrowsWhenNoAuthMethodAvailable() {
        Path tokenFile = tempDir.resolve("nonexistent.json");
        TokenStore store = new TokenStore(tokenFile);

        AuthManager manager = new AuthManager(store, null);
        AuthException ex = assertThrows(AuthException.class, manager::getToken);
        assertTrue(ex.getMessage().contains("No authentication method configured"));
    }

    @Test
    void clearTokenDeletesDiskFile() throws IOException {
        Path tokenFile = tempDir.resolve("token.json");
        TokenStore store = new TokenStore(tokenFile);
        store.save(new Credentials("dupe_disk_token", "my-app", "2026-04-06T12:00:00Z"));

        AuthManager manager = new AuthManager(store, null);
        manager.clearToken();

        assertFalse(tokenFile.toFile().exists());
        assertFalse(manager.hasToken());
    }

    @Test
    void getTokenCachesInMemoryAfterDiskLoad() throws DupeDBException, IOException {
        Path tokenFile = tempDir.resolve("token.json");
        TokenStore store = new TokenStore(tokenFile);
        store.save(new Credentials("dupe_disk_token", "my-app", "2026-04-06T12:00:00Z"));

        AuthManager manager = new AuthManager(store, null);

        // First call loads from disk
        assertEquals("dupe_disk_token", manager.getToken());

        // Delete file -- second call should still return cached token
        store.delete();
        assertEquals("dupe_disk_token", manager.getToken());
    }
}
