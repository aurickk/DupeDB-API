# DupeDB-API

Java library for the DupeDB Minecraft exploit database API. 

## Requirements

- Java 25+

## Installation

Add the JitPack repository and dependency:

**Gradle (Kotlin DSL):**
```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// build.gradle.kts
dependencies {
    implementation("com.github.Aurickk:dupedb-api:1.0.0")
}
```

**Gradle (Groovy DSL):**
```groovy
// settings.gradle
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}

// build.gradle
dependencies {
    implementation 'com.github.Aurickk:dupedb-api:1.0.0'
}
```

## OAuth App Registration

To use authenticated endpoints, you need a registered OAuth app. Open a ticket in the [DupeDB Discord](https://discord.com/invite/J5fQrKVxrC) and provide:

| Field | Description | Example |
|-------|-------------|---------|
| **App ID** | Unique slug (`3-32` lowercase alphanumeric + dashes) | `my-fabric-mod` |
| **App Name** | Shown to users on the consent screen (max 100 chars) | `My Fabric Mod` |
| **Redirect URIs** | Callback URLs (exact match, one per environment) | `http://localhost:9876/callback` |
| **Read-Only** | Whether the app only needs read access | `false` |

The app ID cannot be changed after creation. See the full [Developer Documentation](https://dupedb.net/developers) for details on the OAuth flow, permissions, and token handling.

## Usage

### Unauthenticated (public endpoints only)
```java
DupeDBClient client = DupeDB.client().build();

Exploit exploit = client.exploits().getById("abc12345678");
```

### Authenticated with token (headless servers)
```java
DupeDBClient client = DupeDB.client()
    .token("dupe_your_token_here")
    .build();

SearchResult<ExploitCard> results = client.exploits().search("elytra", 1);
```

### Authenticated with OAuth (desktop/mod usage)
```java
DupeDBClient client = DupeDB.client()
    .oauth("your-app-id", "http://localhost:9876/callback")
    .tokenStore(Path.of("config/dupedb-token.json"))
    .build();

// Opens browser for auth on first use, saves token for future sessions
User me = client.user().me();
```

### Which auth mode to use

| Context | Auth Mode | Why |
|---------|-----------|-----|
| Fabric/Forge client mod | OAuth | Player is at their desktop -- browser flow works naturally. Token is saved so the player only authenticates once. |
| Paper/Spigot server plugin | Token | Servers run headless -- no browser available. Generate a token on dupedb.net and pass it in config. |
| Read-only / public data | None | Public endpoints (metadata, public stats, exploit lookup by slug) need no auth. |

### Threading

All API calls are **blocking HTTP requests**. Never call them on Minecraft's main thread -- this will freeze the game or trigger the server watchdog.

```java
// Fabric example -- run off the main thread
CompletableFuture.supplyAsync(() -> {
    try {
        return client.exploits().getById("abc12345678");
    } catch (DupeDBException e) {
        throw new RuntimeException(e);
    }
}).thenAccept(exploit -> {
    // Back on any thread -- use MinecraftClient.execute() if you need the render thread
    System.out.println(exploit.name());
});
```

### Error handling
```java
try {
    Exploit exploit = client.exploits().getById("abc12345678");
} catch (AuthException e) {
    // Token expired or invalid
} catch (RateLimitException e) {
    // Too many requests -- retry after e.getRetryAfterSeconds()
} catch (ApiException e) {
    // HTTP error: e.getStatusCode(), e.getMessage()
} catch (NetworkException e) {
    // Connection failed
}
```

## API Reference

### Exploits

| Method | Endpoint | Auth |
|--------|----------|------|
| `exploits().search(query, page)` | GET /api/exploits/search | Yes |
| `exploits().search(query, page, filters)` | GET /api/exploits/search | Yes |
| `exploits().getById(id)` | GET /api/exploits/slug/:id | No |
| `exploits().getByIdAuth(id)` | GET /api/exploits/:id | Yes |
| `exploits().update(id, data)` | PUT /api/exploits/:id | Yes |

### Votes

| Method | Endpoint | Auth |
|--------|----------|------|
| `votes().get(exploitId)` | GET /api/exploits/:id/vote | Yes |
| `votes().vote(exploitId, type)` | POST /api/exploits/:id/vote | Yes |

### Comments

| Method | Endpoint | Auth |
|--------|----------|------|
| `comments().list(exploitId)` | GET /api/exploits/:id/comments | Yes |
| `comments().add(exploitId, content)` | POST /api/exploits/:id/comments | Yes |

### Drafts

| Method | Endpoint | Auth |
|--------|----------|------|
| `drafts().getCurrent()` | GET /api/exploits/draft | Yes |
| `drafts().create(data)` | POST /api/exploits/draft | Yes |
| `drafts().update(id, data)` | PUT /api/exploits/draft/:id | Yes |
| `drafts().delete(id)` | DELETE /api/exploits/draft/:id | Yes |
| `drafts().submit(id)` | POST /api/exploits/draft/:id/submit | Yes |

### Current User

| Method | Endpoint | Auth |
|--------|----------|------|
| `user().me()` | GET /api/auth/me | Yes |
| `user().updateDisplayName(name)` | PUT /api/auth/display-name | Yes |
| `user().updatePrivacy(hide)` | PUT /api/auth/privacy-settings | Yes |
| `user().myExploits()` | GET /api/auth/my-exploits | Yes |
| `user().myComments()` | GET /api/auth/my-comments | Yes |
| `user().deleteExploit(id)` | DELETE /api/auth/my-exploits/:id | Yes |
| `user().connectedApps()` | GET /api/oauth/connected | Yes |
| `user().revokeApp(appId)` | DELETE /api/oauth/connected/:appId | Yes |
| `user().deleteAccount()` | DELETE /api/auth/account | Yes |

### User Profiles

| Method | Endpoint | Auth |
|--------|----------|------|
| `users().getProfile(userId)` | GET /api/users/:id/profile | No |
| `users().lookup(name)` | GET /api/users/lookup/:name | No |

### Communities

| Method | Endpoint | Auth |
|--------|----------|------|
| `communities().list()` | GET /api/communities/ | No |
| `communities().detect(url)` | GET /api/communities/detect | No |
| `communities().getById(id)` | GET /api/communities/:id | No |

### Media

| Method | Endpoint | Auth |
|--------|----------|------|
| `media().uploadProfilePicture(file)` | POST /api/auth/profile-picture | Yes |
| `media().deleteProfilePicture()` | DELETE /api/auth/profile-picture | Yes |
| `media().uploadMedia(file)` | POST /api/upload/media | Yes |
| `media().uploadMedia(file, draftId, exploitId, uploadType, resourceId)` | POST /api/upload/media | Yes |
| `media().deleteMedia(filename)` | DELETE /api/upload/media/:filename | Yes |

### Resources

| Method | Endpoint | Auth |
|--------|----------|------|
| `resources().categories()` | GET /api/resources/categories | No |
| `resources().list()` | GET /api/resources/ | No |
| `resources().list(categoryId)` | GET /api/resources/?category=:id | No |
| `resources().getById(id)` | GET /api/resources/id/:id | No |
| `resources().getBySlug(slug)` | GET /api/resources/:slug | No |

### Metadata

| Method | Endpoint | Auth |
|--------|----------|------|
| `metadata().health()` | GET /api/health | No |
| `metadata().version()` | GET /api/version | No |
| `metadata().tags()` | GET /api/tags | No |
| `metadata().versions()` | GET /api/versions | No |
| `metadata().types()` | GET /api/types | No |
| `metadata().stats()` | GET /api/stats | No |
| `metadata().latestActivity()` | GET /api/latest-activity | No |
| `metadata().serverIps(page, limit)` | GET /api/server-ips | No |
| `metadata().plugins()` | GET /api/plugins | No |
| `metadata().publicStats()` | GET /api/public/stats | No |
| `metadata().publicStatsHistory(days)` | GET /api/public/stats/history | No |
| `metadata().siteVisibility()` | GET /api/site-visibility | No |
| `metadata().publicExploits()` | GET /api/public/exploits | No |

## Rate Limits

| Endpoint Group | Limit |
|----------------|-------|
| General | 200 / 15 min |
| Search (browse) | 30 / min |
| Detail | 15 / min |
| Voting | 60 / min |
| Comments | 10 / min |
| Draft submit | 20 / hour |
