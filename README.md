# Spring Boot Starter Template

This project provides an opinionated Spring Boot starter template that implements production-ready defaults for building
modular, multi-tenant ready REST APIs. It follows the API design rules that emphasise deterministic responses,
idempotent writes, RBAC/ABAC authorisation, centralised error handling, and observability-friendly logging.

## ✨ Highlights

- **Modular architecture** with domain-specific modules under `modules/` (e.g. `users`) and cross-cutting concerns in
  `common/`.
- **Shared base models** providing numeric ids, audit timestamps, and DTO metadata via `BaseEntity`/`BaseDto`.
- **Keycloak Authentication & OAuth2**: Industry-standard OAuth2/OpenID Connect integration with Keycloak for user management,
  SSO, and social login (Google). Falls back to legacy JWT mode when Keycloak is disabled.
- **RBAC + ABAC**: Role checks are enforced via Spring Security annotations while resource ownership checks are
  delegated to dedicated guards. Roles can be managed in Keycloak or locally.
- **Idempotent write endpoints** using the `@Idempotent` annotation and the `idempotency_keys` table.
- **Rate limiting** built on Bucket4j with user/IP/device granularity and configurable quotas.
- **Global error contract** that always returns a deterministic payload containing `code`, `message`, `traceId`, and
  `timestamp`.
- **Trace propagation** via `X-Trace-Id` header coupled with MDC logging enrichment.
- **OpenAPI 3 documentation** powered by Springdoc at `/swagger-ui/index.html`.
- **ModelMapper integration** for DTO ↔ entity transformations.
- **Ready-to-use testing profile** backed by an in-memory H2 database.
- **Centralised media storage** on Amazon S3 with automatic image variants and manifest metadata.
- **Real-time notifications** delivered through a dedicated microservice backed by RabbitMQ and WebSockets.

---

## 🌐 Geliştirici Servisleri

| Service                          | URL                                                                                        | Description                   | Credentials     |
|----------------------------------|--------------------------------------------------------------------------------------------|-------------------------------|-----------------|
| **Swagger (Gateway)**            | [http://localhost:8088/swagger-ui/index.html](http://localhost:8088/swagger-ui/index.html) | API dokümantasyonu            | -               |
| **Keycloak Admin Console**       | [http://localhost:9090](http://localhost:9090)                                             | Identity & Access Management  | admin/admin     |
| **Redis Insight**                | [http://localhost:5540](http://localhost:5540)                                             | Redis yönetim arayüzü         | -               |
| **RabbitMQ Management**          | [http://localhost:15672](http://localhost:15672)                                           | Queue Management              | guest/guest     |
| **Notification Service Swagger** | [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html) | Notification microservice API | -               |
| **Elasticsearch**                | [http://localhost:9200](http://localhost:9200)                                             | Search & Index Service        | elastic/changeme|
| **Kibana**                       | [http://localhost:5601](http://localhost:5601)                                             | Log Interface                 | elastic/changeme|
| **APM Server**                   | [http://localhost:8200](http://localhost:8200)                                             | APM Server                    | -               |

---

## 🧱 Microservice Architecture

```mermaid
flowchart LR
subgraph Core Platform
A[Gateway API\\n(spring-boot-starter-template)]
end
subgraph Messaging
MQ[(RabbitMQ\\nnotifications.exchange)]
end
subgraph Realtime Delivery
N[Notification Service\\n(WebSocket + REST)]
end
DB[(PostgreSQL)]
REDIS[(Redis)]

A -- JPA --> DB
A -- Cache --> REDIS
A -- NotificationMessage --> MQ
MQ -- Async consume --> N
N -- Persist --> DB
N -- Push --> Clients[/WebSocket subscribers/]
```

**Microservice Hot Reload**

```bash
docker-compose watch
```

---

### Kibana Setup

**Generate Kibana Service Token:**

```bash
docker exec -it spring-boot-starter-elasticsearch \
  bin/elasticsearch-service-tokens create elastic/kibana kibana-token
```

**Create Kibana Service Password:**

```bash
docker exec -it spring-boot-starter-elasticsearch bash
```

## 📦 Project Structure

```text
src/main/java/com/autumnus/spring_boot_starter_template
├── SpringBootStarterTemplateApplication.java
├── common
│   ├── api/              # API response wrappers
│   ├── config/           # Configuration properties and OpenAPI settings
│   ├── context/          # Request context holder utilities
│   ├── exception/        # Error model and global exception handler
│   ├── idempotency/      # @Idempotent aspect and persistence layer
│   ├── logging/          # Trace id filter and MDC integration
│   ├── persistence/      # Base JPA entities
│   ├── rate_limiting/    # Bucket4j backed rate limiting filter/service
│   └── security/         # JWT utilities, filter, and security helpers
└── modules
    └── users
        ├── controller/   # REST controller exposing versioned endpoints
        ├── dto/          # Request/response DTOs
        ├── entity/       # JPA entities and enums
        ├── mapper/       # ModelMapper powered converters
        ├── repository/   # Spring Data repositories & specifications
        └── service/      # Service interfaces + implementations
```

```text
notification-service
├── NotificationServiceApplication.java
├── common/              # Shared context, API and exception handling
├── config/              # RabbitMQ, WebSocket, OpenAPI configuration
└── modules/notifications
    ├── controller/      # REST endpoints under /api/v1/notifications
    ├── dto/             # API and messaging DTOs
    ├── entity/          # Notification JPA entities
    ├── listener/        # RabbitMQ listeners & WebSocket publisher
    ├── mapper/          # Entity → DTO mappers
    └── service/         # Notification domain services
```

## ⚙️ Configuration

Configuration is managed through `application.yaml` and can be overridden via environment variables or profile-specific
YAML files.

| Property                                           | Description                                                                     |
|----------------------------------------------------|---------------------------------------------------------------------------------|
| `application.security.jwt-secret`                  | HMAC secret for signing JWT access tokens (min 32 chars).                       |
| `application.security.access-token-ttl`            | Duration (ISO-8601) for access token lifetime.                                  |
| `application.security.public-endpoints`            | Comma-separated list of patterns that bypass authentication.                    |
| `application.rate-limit.capacity`                  | Maximum number of requests permitted per refill period.                         |
| `application.rate-limit.refill-period`             | ISO-8601 duration describing the bucket refill cadence.                         |
| `application.storage.s3.bucket`                    | Target S3 bucket that will store static assets.                                 |
| `application.storage.s3.region`                    | AWS region of the bucket (e.g. `eu-central-1`).                                 |
| `application.storage.s3.access-key` / `secret-key` | Optional explicit credentials; falls back to default provider chain if omitted. |
| `application.storage.s3.endpoint`                  | Optional custom endpoint (e.g. Localstack).                                     |
| `application.storage.s3.path-style-access`         | Enable when interacting with Localstack/minio style endpoints.                  |
| `application.storage.s3.public-base-url`           | Optional CDN/public URL prefix used when building asset links.                  |
| `application.messaging.notifications.*`            | Exchange, queue and routing key used for RabbitMQ-based notification fan-out.   |
| `spring.datasource.*`                              | Database connectivity settings (PostgreSQL by default).                         |
| `spring.data.redis.*`                              | Redis connection info for caching / distributed tokens (optional).              |
| `spring.rabbitmq.*`                                | RabbitMQ host, port and credentials shared with the notification microservice.  |

> A dedicated `application-test.yaml` configures an in-memory H2 database and a deterministic JWT secret for test runs.

## 📁 Media Storage

Static assets (images, audio, video, documents) are uploaded to Amazon S3 through the `MediaStorageService`. Files are
organised with the following layout:

```
media/{kind}/{purpose}/{yyyy}/{mm}/{dd}/{sha12}-{uuid}/
  original.{ext}
  manifest.json
  variants/
    web.{ext}
    mobile.{ext}
    thumb.{ext}
```

- `kind` reflects the media category (`image`, `video`, `audio`, `document`).
- `purpose` is a lowercase slug (e.g. `avatar`, `cover`, `post`).
- `sha12` is derived from the original content hash to improve deduplication, followed by a random UUID.
- `manifest.json` captures the full metadata of the original asset and every generated variant.

### Image Variants

Images (`image/jpeg`, `image/png`) are validated to be ≤ 10 MB and automatically produce:

| Variant    | Max Dimensions      | Suggested Usage                  |
|------------|---------------------|----------------------------------|
| `original` | Original resolution | Archival/original downloads      |
| `web`      | 1920×1080           | Desktop web experiences          |
| `mobile`   | 1080×1080           | Handset/tablet friendly previews |
| `thumb`    | 320×320             | Avatars, list thumbnails         |

PDF documents are limited to 25 MB. Other media kinds can be extended with additional validation rules as needed.

To upload multiple images in a single call, use the `MediaStorageService#storeAll` API which enforces a batch size of
1–100 files and applies the same validation/variant pipeline to each item.

### User Profile & Photos

The `users` module stores the media manifest JSON directly on the `users.profile_photo_manifest` column. Each
`UserResponse` now exposes the numeric `id`, audit timestamps, and a `profilePhoto` object with public URLs for every
variant.

Swagger documents both administrative and self-service flows:

- `/api/v1/users/{id}` endpoints remain available for elevated roles that manage other accounts.
- `/api/v1/users/me` endpoints allow authenticated users to fetch and mutate their own profile without providing an id:
    - `GET /api/v1/users/me` — retrieve your profile.
    - `PUT /api/v1/users/me` — update your email/username.
    - `POST /api/v1/users/me/profile-photo` — upload or replace your avatar (multipart `file`, PNG/JPEG ≤ 10 MB).
    - `DELETE /api/v1/users/me/profile-photo` — remove your avatar and purge stored objects.

Once authorised in Swagger UI, select the self-service operations to update your profile—the backend extracts immutable
identifiers directly from the JWT payload.

## 🔐 Security Model

### Keycloak Mode (Recommended)

When `application.security.keycloak.enabled=true`:

- **Authentication:** OAuth2 Resource Server validates JWT tokens issued by Keycloak. Requests must carry a `Bearer <token>` header.
- **User Registration:** Creates users in both Keycloak and local database, synchronized via `keycloak_id`.
- **Login:** Direct grant flow against Keycloak token endpoint returns standard OAuth2 tokens.
- **Token Refresh:** Keycloak manages token rotation and refresh logic.
- **Social Login:** Google OAuth2 configured as Identity Provider in Keycloak realm.
- **Roles:** Managed in Keycloak realm roles and synchronized to local database for compatibility.
- **Admin Operations:** Keycloak Admin Client allows programmatic user management (create, update, assign roles).

### Legacy JWT Mode

When Keycloak is disabled (`application.security.keycloak.enabled=false`):

- **Authentication:** Custom JWT provider signs tokens with configured secret.
- **Token payload:** Access and refresh tokens embed user id, email, username, and role claims.
- **Refresh Tokens:** Stored in local database with device tracking and rotation support.

### Common Features

- **Authorisation:**
    - RBAC checks rely on Spring Security's `@PreAuthorize`/`@PostAuthorize` annotations (e.g. `hasRole('ADMIN')`).
    - ABAC checks leverage helper beans such as `OwnershipGuard` for owner-scoped access (`@ownershipGuard.isOwner(#id)`).
- **Context propagation:** Authenticated user id is stored in `RequestContext` for rate limiting and auditing.

## 🔁 Idempotency

Annotate write operations with `@Idempotent` to ensure repeatable outcomes.

```java

@PostMapping
@Idempotent
public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
    // ...
}
```

Clients must provide a unique `Idempotency-Key` header per logical request. The aspect serialises the method arguments,
stores the response body + status inside the `idempotency_keys` table, and replays the cached response for identical
repeats. Conflicting payloads using the same key yield a `409 CONFLICT` error with code `IDEMPOTENCY_KEY_CONFLICT`.

## 🚦 Rate Limiting

Bucket4j guards every request with a composite key built from user id, IP address, device id (`X-Device-Id` header),
HTTP method, and request path. Exceeding the quota emits a `429 Too Many Requests` response with the standard error
payload and a `Retry-After` header derived from configuration.

## 📚 API Documentation

Springdoc automatically exposes:

- OpenAPI spec: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui/index.html`

The documentation includes JWT bearer authentication information, enabling quick testing once a token is supplied via
the UI's authorise dialog.

## 🧪 Testing

- Unit/integration tests should be executed via `./mvnw test`.
- The `test` profile spins up with an in-memory H2 database and a deterministic JWT secret.

> **Note:** The Maven wrapper downloads Maven on first run. Ensure outbound network access is available or install Maven
> locally if the wrapper cannot fetch the distribution.

## 🔑 Keycloak Setup

### Initial Configuration

1. **Environment Variables:**
   Copy `.env.example` to `.env` and configure:
   ```bash
   KEYCLOAK_ADMIN_USER=admin
   KEYCLOAK_ADMIN_PASSWORD=admin
   KEYCLOAK_DB_NAME=keycloak
   KEYCLOAK_REALM=spring-boot-starter
   KEYCLOAK_RESOURCE=spring-boot-app
   ```

2. **Start Keycloak:**
   ```bash
   docker compose up -d keycloak
   ```

3. **Access Admin Console:**
   Navigate to [http://localhost:9090](http://localhost:9090) and login with admin credentials.

### Realm Configuration

The realm `spring-boot-starter` is auto-imported from `keycloak/realm-export.json` including:

- **Roles:** ADMIN, USER, MODERATOR
- **Client:** `spring-boot-app` with Direct Grant enabled
- **Default Users:**
    - admin@example.com / admin123 (ADMIN, USER roles)
    - user@example.com / user123 (USER role)
- **Google Identity Provider:** Pre-configured (requires client ID/secret)

### Google OAuth2 Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create OAuth2 credentials
3. Add authorized redirect URI: `http://localhost:9090/realms/spring-boot-starter/broker/google/endpoint`
4. In Keycloak Admin Console:
    - Navigate to `Identity Providers` → `google`
    - Add Client ID and Client Secret
    - Save configuration

### Client Secret Generation

After first startup, retrieve the client secret:

1. Admin Console → Clients → `spring-boot-app`
2. Credentials tab → Copy secret
3. Update `.env`:
   ```bash
   KEYCLOAK_CREDENTIALS_SECRET=<your-client-secret>
   ```
4. Restart application

## 🚀 Getting Started

1. **Install dependencies:** Java 17+, Docker (for infrastructure services).
2. **Configure environment:**
   - Copy `.env.example` to `.env`
   - Update database credentials, Keycloak settings, and AWS S3 config
   - For legacy JWT mode, set `application.security.keycloak.enabled=false`
3. **Run infrastructure:**
   ```bash
   docker compose up -d
   ```
4. **Launch the application:**
   ```bash
   ./mvnw spring-boot:run
   ```
5. **Access Swagger UI:**
   Open [http://localhost:8088/swagger-ui/index.html](http://localhost:8088/swagger-ui/index.html)

### Example Request Flow

```bash
# Create user (idempotent)
curl -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer <JWT>" \
  -H "Idempotency-Key: 0b4d37a1-3d95-4b4a-92f1-8c1f0de733cd" \
  -H "Content-Type: application/json" \
  -d '{
        "email": "user@example.com",
        "displayName": "Example User",
        "status": "ACTIVE",
        "roles": ["ROLE_USER"]
      }'
```

Repeat the same call with the identical key to receive the cached `201 Created` response.

## 🗺️ Extending the Template

1. Create a new module under `modules/<domain>` mirroring the `users` module layout.
2. Define DTOs, entities, repositories, and services adhering to the interface-first rule.
3. Expose versioned controllers under `/api/v1/<resource>` and reuse shared components from `common/`.
4. Apply `@Idempotent` to mutation endpoints and secure them with RBAC/ABAC annotations.
5. Document the endpoints via Javadoc and rely on Springdoc for runtime OpenAPI generation.

## 🧰 Tooling & Libraries

- Spring Boot 3.5.x
- Spring Security, Validation, Data JPA
- Bucket4j (rate limiting)
- ModelMapper (mapping)
- JJWT (JWT signing)
- Springdoc OpenAPI
- Lombok

## 📄 License

This starter is provided as-is. Adapt, extend, and integrate it into your own services as needed.
