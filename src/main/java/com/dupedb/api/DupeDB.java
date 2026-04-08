package com.dupedb.api;

import com.dupedb.api.auth.AuthManager;
import com.dupedb.api.auth.OAuthFlow;
import com.dupedb.api.auth.TokenStore;

import java.nio.file.Path;

/** Static entry point for constructing a {@link DupeDBClient}. Default base URL: {@code https://dupedb.net}. */
public final class DupeDB {
    private static final String DEFAULT_BASE_URL = "https://dupedb.net";

    private DupeDB() {} // No instantiation

    /** Creates a new builder for constructing a {@link DupeDBClient}. */
    public static Builder client() {
        return new Builder();
    }

    /** Fluent builder for {@link DupeDBClient}. Supports unauthenticated, token, or OAuth modes. */
    public static class Builder {
        private String baseUrl = DEFAULT_BASE_URL;
        private String token;
        private String oauthAppId;
        private String oauthRedirectUri;
        private Path tokenStorePath;

        Builder() {}

        /** Sets the API base URL. */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /** Sets a pre-configured token for headless server mode. */
        public Builder token(String token) {
            this.token = token;
            return this;
        }

        /** Configures OAuth browser flow. Triggered lazily on first authenticated call. */
        public Builder oauth(String appId, String redirectUri) {
            this.oauthAppId = appId;
            this.oauthRedirectUri = redirectUri;
            return this;
        }

        /** Sets a custom token storage path. Default: {@code ~/.dupedb/token.json}. */
        public Builder tokenStore(Path path) {
            this.tokenStorePath = path;
            return this;
        }

        /** Builds the {@link DupeDBClient}. */
        public DupeDBClient build() {
            if (baseUrl == null || baseUrl.isBlank()) {
                throw new IllegalStateException("baseUrl is required");
            }

            AuthManager authManager = null;

            if (token != null) {
                // Direct token mode (D-05)
                authManager = new AuthManager(token);
            } else if (oauthAppId != null) {
                // OAuth browser flow mode (D-04)
                int port = parsePortFromRedirectUri(oauthRedirectUri);
                OAuthFlow flow = new OAuthFlow(baseUrl, oauthAppId, port);
                TokenStore store = tokenStorePath != null
                    ? new TokenStore(tokenStorePath)
                    : new TokenStore();
                authManager = new AuthManager(store, flow);
            }
            // else: unauthenticated mode (public endpoints only)

            return new DupeDBClient(baseUrl, authManager);
        }

        /** Extracts port from redirect URI; defaults to 9876. */
        private static int parsePortFromRedirectUri(String uri) {
            try {
                int port = java.net.URI.create(uri).getPort();
                return port > 0 ? port : 9876;
            } catch (Exception e) {
                return 9876;
            }
        }
    }
}
