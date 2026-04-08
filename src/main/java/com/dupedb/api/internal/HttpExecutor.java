package com.dupedb.api.internal;

import com.dupedb.api.exception.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/** Internal HTTP client wrapper. Handles auth headers, JSON (de)serialization, and error mapping. */
public class HttpExecutor {
    private final String baseUrl;
    private final Supplier<String> tokenSupplier;
    private final HttpClient httpClient;

    /**
     * Creates a new HttpExecutor.
     *
     * @param baseUrl       the API base URL (e.g. "https://dupedb.net")
     * @param tokenSupplier nullable; resolves the current auth token per-request
     */
    public HttpExecutor(String baseUrl, Supplier<String> tokenSupplier) {
        this.baseUrl = baseUrl;
        this.tokenSupplier = tokenSupplier;
        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    }

    /**
     * Sends a GET request and deserializes the response body.
     *
     * @param path the API path (appended to baseUrl)
     * @param type the class to deserialize into
     * @param <T>  the response type
     * @return the deserialized response
     * @throws DupeDBException if the request fails
     */
    public <T> T get(String path, Class<T> type) throws DupeDBException {
        HttpRequest request = buildRequest(path)
            .GET()
            .build();
        return execute(request, type);
    }

    /**
     * Sends a GET request and deserializes the response into a generic type.
     * Use with {@code TypeToken} for parameterized types like {@code SearchResult<Exploit>}.
     *
     * @param path the API path (appended to baseUrl)
     * @param type the generic type to deserialize into
     * @param <T>  the response type
     * @return the deserialized response
     * @throws DupeDBException if the request fails
     */
    public <T> T get(String path, Type type) throws DupeDBException {
        HttpRequest request = buildRequest(path)
            .GET()
            .build();
        return execute(request, type);
    }

    /**
     * Sends a POST request with a JSON body and deserializes the response.
     *
     * @param path the API path (appended to baseUrl)
     * @param body the request body to serialize as JSON
     * @param type the class to deserialize the response into
     * @param <T>  the response type
     * @return the deserialized response
     * @throws DupeDBException if the request fails
     */
    public <T> T post(String path, Object body, Class<T> type) throws DupeDBException {
        String json = JsonHelper.toJson(body);
        HttpRequest request = buildRequest(path)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        return execute(request, type);
    }

    /**
     * Sends a PUT request with a JSON body and deserializes the response.
     *
     * @param path the API path (appended to baseUrl)
     * @param body the request body to serialize as JSON
     * @param type the class to deserialize the response into
     * @param <T>  the response type
     * @return the deserialized response
     * @throws DupeDBException if the request fails
     */
    public <T> T put(String path, Object body, Class<T> type) throws DupeDBException {
        String json = JsonHelper.toJson(body);
        HttpRequest request = buildRequest(path)
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(json))
            .build();
        return execute(request, type);
    }

    /**
     * Sends a DELETE request. No response body is expected.
     *
     * @param path the API path (appended to baseUrl)
     * @throws DupeDBException if the request fails
     */
    public void delete(String path) throws DupeDBException {
        HttpRequest request = buildRequest(path)
            .DELETE()
            .build();
        execute(request, Void.class);
    }

    /**
     * Sends a DELETE request and deserializes the response body.
     * Use for DELETE endpoints that return JSON (e.g. confirmation with freed size).
     *
     * @param path the API path (appended to baseUrl)
     * @param type the class to deserialize the response into
     * @param <T>  the response type
     * @return the deserialized response
     * @throws DupeDBException if the request fails
     */
    public <T> T deleteWithResponse(String path, Class<T> type) throws DupeDBException {
        HttpRequest request = buildRequest(path)
            .DELETE()
            .build();
        return execute(request, type);
    }

    /**
     * Sends a POST request with a multipart/form-data body and deserializes the response.
     * Use for file upload endpoints.
     *
     * <p>The parts map accepts two value types:
     * <ul>
     *   <li>{@link Path} — sent as a file part with content type probed from the file</li>
     *   <li>{@link String} — sent as a text field</li>
     * </ul>
     *
     * @param path  the API path (appended to baseUrl)
     * @param parts map of field name to value (Path for files, String for text)
     * @param type  the class to deserialize the response into
     * @param <T>   the response type
     * @return the deserialized response
     * @throws DupeDBException if the request fails
     */
    public <T> T postMultipart(String path, Map<String, Object> parts, Class<T> type)
            throws DupeDBException {
        String boundary = "DupeDB-" + UUID.randomUUID();
        HttpRequest request = buildRequest(path)
            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
            .POST(buildMultipartBody(boundary, parts))
            .build();
        return execute(request, type);
    }

    // --- Package-private methods for testing ---

    /**
     * Builds the request headers map. Exposed for testing token resolution behavior.
     *
     * @param contentType nullable content type (set for POST/PUT)
     * @return map of header name to value
     */
    Map<String, String> buildHeaders(String contentType) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/json");

        if (tokenSupplier != null) {
            String token = tokenSupplier.get();
            if (token != null) {
                headers.put("X-App-Token", token);
            }
        }

        if (contentType != null) {
            headers.put("Content-Type", contentType);
        }

        return headers;
    }

    /**
     * Maps an HTTP response to either a deserialized result or an exception.
     * Exposed as package-private for direct testing of the mapping logic.
     *
     * @param statusCode the HTTP status code
     * @param body       the response body string
     * @param headers    the response headers
     * @param type       the class to deserialize success responses into
     * @param <T>        the response type
     * @return the deserialized response, or null for 204/Void
     * @throws DupeDBException the appropriate exception for error status codes
     */
    <T> T mapResponse(int statusCode, String body, HttpHeaders headers, Class<T> type)
            throws DupeDBException {
        return switch (statusCode) {
            case 200, 201 -> {
                if (type == Void.class || body == null || body.isEmpty()) {
                    yield null;
                }
                yield JsonHelper.fromJson(body, type);
            }
            case 204 -> null;
            case 401 -> throw new AuthException(parseErrorMessage(body));
            case 403 -> throw new AuthException(parseErrorMessage(body));
            case 429 -> throw new RateLimitException(parseRetryAfter(headers));
            default -> {
                if (statusCode >= 400) {
                    throw new ApiException(statusCode, parseErrorMessage(body));
                }
                // For any other 2xx codes, try to deserialize
                if (type == Void.class || body == null || body.isEmpty()) {
                    yield null;
                }
                yield JsonHelper.fromJson(body, type);
            }
        };
    }

    /**
     * Wraps an IOException as a NetworkException. Exposed for testing.
     *
     * @param e the IOException to wrap
     * @throws NetworkException always thrown
     */
    void wrapIOException(IOException e) throws NetworkException {
        throw new NetworkException("Connection failed", e);
    }

    /**
     * Wraps an InterruptedException as a NetworkException and re-sets the interrupt flag.
     * Exposed for testing.
     *
     * @param e the InterruptedException to wrap
     * @throws NetworkException always thrown
     */
    void wrapInterruptedException(InterruptedException e) throws NetworkException {
        Thread.currentThread().interrupt();
        throw new NetworkException("Request interrupted", e);
    }

    // --- Private implementation ---

    private HttpRequest.BodyPublisher buildMultipartBody(String boundary, Map<String, Object> parts)
            throws DupeDBException {
        List<byte[]> byteArrays = new ArrayList<>();
        byte[] separator = ("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8);

        try {
            for (Map.Entry<String, Object> entry : parts.entrySet()) {
                byteArrays.add(separator);
                String name = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof Path filePath) {
                    String filename = filePath.getFileName().toString();
                    String contentType = Files.probeContentType(filePath);
                    if (contentType == null) {
                        contentType = "application/octet-stream";
                    }
                    byteArrays.add(("Content-Disposition: form-data; name=\"" + name
                        + "\"; filename=\"" + filename + "\"\r\n"
                        + "Content-Type: " + contentType + "\r\n\r\n")
                        .getBytes(StandardCharsets.UTF_8));
                    byteArrays.add(Files.readAllBytes(filePath));
                } else {
                    byteArrays.add(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n"
                        + value).getBytes(StandardCharsets.UTF_8));
                }
                byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
            }
            byteArrays.add(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new NetworkException("Failed to read file for upload", e);
        }

        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }

    private HttpRequest.Builder buildRequest(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .header("Accept", "application/json")
            .timeout(Duration.ofSeconds(30));

        if (tokenSupplier != null) {
            String token = tokenSupplier.get();
            if (token != null) {
                builder.header("X-App-Token", token);
            }
        }

        return builder;
    }

    @SuppressWarnings("unchecked")
    private <T> T execute(HttpRequest request, Class<T> type) throws DupeDBException {
        try {
            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );
            return mapResponse(response.statusCode(), response.body(), response.headers(), type);
        } catch (IOException e) {
            throw new NetworkException("Connection failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NetworkException("Request interrupted", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T execute(HttpRequest request, Type type) throws DupeDBException {
        try {
            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );
            int statusCode = response.statusCode();
            String body = response.body();
            HttpHeaders headers = response.headers();

            return switch (statusCode) {
                case 200, 201 -> {
                    if (body == null || body.isEmpty()) {
                        yield null;
                    }
                    yield JsonHelper.fromJson(body, type);
                }
                case 204 -> null;
                case 401 -> throw new AuthException(parseErrorMessage(body));
                case 403 -> throw new AuthException(parseErrorMessage(body));
                case 429 -> throw new RateLimitException(parseRetryAfter(headers));
                default -> {
                    if (statusCode >= 400) {
                        throw new ApiException(statusCode, parseErrorMessage(body));
                    }
                    if (body == null || body.isEmpty()) {
                        yield null;
                    }
                    yield JsonHelper.fromJson(body, type);
                }
            };
        } catch (IOException e) {
            throw new NetworkException("Connection failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NetworkException("Request interrupted", e);
        }
    }

    /**
     * Parses an error message from a JSON response body.
     * The DupeDB server returns errors as {@code {"error": "message"}}.
     * Falls back to the raw body if JSON parsing fails.
     */
    private String parseErrorMessage(String body) {
        if (body == null || body.isEmpty()) {
            return "Unknown error";
        }
        try {
            JsonObject obj = JsonParser.parseString(body).getAsJsonObject();
            if (obj.has("error")) {
                return obj.get("error").getAsString();
            }
        } catch (JsonSyntaxException | IllegalStateException e) {
            // Not valid JSON or not an object -- fall through to raw body
        }
        return body;
    }

    /**
     * Parses the RateLimit-Reset header to determine retry-after seconds.
     * Defaults to 60 seconds if the header is missing or unparseable.
     */
    private int parseRetryAfter(HttpHeaders headers) {
        return headers.firstValue("RateLimit-Reset")
            .map(value -> {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return 60;
                }
            })
            .orElse(60);
    }
}
