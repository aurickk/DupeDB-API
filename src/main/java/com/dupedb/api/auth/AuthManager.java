package com.dupedb.api.auth;

import com.dupedb.api.exception.AuthException;
import com.dupedb.api.exception.DupeDBException;

import java.io.IOException;
import java.time.Instant;

/**
 * Manages auth token lifecycle. Resolves tokens from: memory, disk, or OAuth flow (in that order).
 */
public class AuthManager {
    private final TokenStore tokenStore;
    private final OAuthFlow oAuthFlow; // null if using direct token (D-05)
    private String currentToken;

    /** Creates an AuthManager for the OAuth browser flow. */
    public AuthManager(TokenStore tokenStore, OAuthFlow oAuthFlow) {
        this.tokenStore = tokenStore;
        this.oAuthFlow = oAuthFlow;
        this.currentToken = null;
    }

    /** Creates an AuthManager with a pre-configured token (no OAuth, no disk). */
    public AuthManager(String token) {
        this.tokenStore = null;
        this.oAuthFlow = null;
        this.currentToken = token;
    }

    /** Gets a valid token, resolving from memory -> disk -> OAuth flow. Thread-safe. */
    public synchronized String getToken() throws DupeDBException {
        if (currentToken != null) {
            return currentToken;
        }

        if (tokenStore != null) {
            Credentials stored = tokenStore.load();
            if (stored != null && stored.token() != null) {
                currentToken = stored.token();
                return currentToken;
            }
        }

        if (oAuthFlow != null) {
            String token = oAuthFlow.authenticate();
            currentToken = token;

            if (tokenStore != null) {
                try {
                    tokenStore.save(new Credentials(
                        token,
                        oAuthFlow.getAppId(),
                        Instant.now().toString()
                    ));
                } catch (IOException ignored) {
                }
            }

            return currentToken;
        }

        throw new AuthException("No authentication method configured. "
            + "Use .token() for a pre-configured token or .oauth() for browser flow.");
    }

    /** Clears the token from memory and disk. Thread-safe. */
    public synchronized void clearToken() {
        currentToken = null;
        if (tokenStore != null) {
            tokenStore.delete();
        }
    }

    /** Returns whether a token is available without triggering auth. Thread-safe. */
    public synchronized boolean hasToken() {
        if (currentToken != null) return true;
        if (tokenStore != null) {
            Credentials stored = tokenStore.load();
            return stored != null && stored.token() != null;
        }
        return false;
    }
}
