package com.dupedb.api.auth;

import com.dupedb.api.exception.OAuthException;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Executes the DupeDB OAuth browser flow to obtain an authentication token.
 * The {@code ?code=} parameter IS the actual {@code dupe_} token — no exchange step.
 */
public class OAuthFlow {
    private final String baseUrl;
    private final String appId;
    private final int callbackPort;

    private static final int DEFAULT_PORT = 9876;
    private static final int TIMEOUT_SECONDS = 300; // 5 minutes, matching server's pending auth TTL

    /** Creates an OAuthFlow using the default callback port (9876). */
    public OAuthFlow(String baseUrl, String appId) {
        this(baseUrl, appId, DEFAULT_PORT);
    }

    /** Creates an OAuthFlow with a custom callback port. */
    public OAuthFlow(String baseUrl, String appId, int callbackPort) {
        this.baseUrl = baseUrl;
        this.appId = appId;
        this.callbackPort = callbackPort;
    }

    /** Executes the full OAuth flow: opens browser, waits for callback, returns token. */
    public String authenticate() throws OAuthException {
        CompletableFuture<String> tokenFuture = new CompletableFuture<>();
        HttpServer server = null;

        try {
            // Start localhost callback server
            server = HttpServer.create(new InetSocketAddress(callbackPort), 0);
            final HttpServer srv = server;

            server.createContext("/callback", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                // CRITICAL: ?code= IS the token. No exchange needed.
                String token = parseParam(query, "code");
                String error = parseParam(query, "error");

                // Send HTML response to browser
                String html;
                if (token != null) {
                    html = """
                        <html><body style="font-family:system-ui;text-align:center;padding:50px">
                        <h1>Authenticated</h1>
                        <p>You can close this tab and return to your application.</p>
                        </body></html>""";
                } else {
                    html = """
                        <html><body style="font-family:system-ui;text-align:center;padding:50px">
                        <h1>Authentication Failed</h1>
                        <p>Authorization was denied or an error occurred.</p>
                        </body></html>""";
                }

                byte[] response = html.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
                exchange.close();

                if (token != null) {
                    tokenFuture.complete(token);
                } else {
                    tokenFuture.completeExceptionally(
                        new OAuthException("Authorization denied" + (error != null ? ": " + error : "")));
                }

                srv.stop(1);
            });

            server.start();

            // Build authorize URL and open browser
            String redirectUri = "http://localhost:" + callbackPort + "/callback";
            String authorizeUrl = baseUrl + "/api/oauth/authorize?app_id=" + appId
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(authorizeUrl));
            } else {
                throw new OAuthException("Cannot open browser. Desktop.browse() not supported on this platform. "
                    + "Use a pre-configured token instead.");
            }

            // Wait for callback (5 min timeout matching server's pending auth TTL)
            return tokenFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        } catch (OAuthException e) {
            throw e;
        } catch (TimeoutException e) {
            throw new OAuthException("OAuth flow timed out after " + TIMEOUT_SECONDS + " seconds");
        } catch (ExecutionException e) {
            if (e.getCause() instanceof OAuthException oe) throw oe;
            throw new OAuthException("OAuth flow failed: " + e.getMessage());
        } catch (Exception e) {
            throw new OAuthException("OAuth flow failed: " + e.getMessage(), e);
        } finally {
            if (server != null) {
                server.stop(0);
            }
        }
    }

    /** Parses a query parameter value from a URL query string. */
    static String parseParam(String query, String name) {
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && kv[0].equals(name)) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    public String getAppId() { return appId; }
    public int getCallbackPort() { return callbackPort; }
}
