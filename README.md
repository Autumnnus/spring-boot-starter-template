# Spring Boot Starter Template

This project provides an opinionated Spring Boot starter template that implements production-ready defaults for building modular, multi-tenant ready REST APIs. It follows the API design rules that emphasise deterministic responses, idempotent writes, RBAC/ABAC authorisation, centralised error handling, and observability-friendly logging.

The project now demonstrates **microservices architecture** with a dedicated real-time notification service that communicates asynchronously via RabbitMQ.

## ✨ Highlights

- **Microservices architecture** with independent services communicating via message queues (RabbitMQ)
- **Modular architecture** with domain-specific modules under `modules/` (e.g. `users`) and cross-cutting concerns in `common/`.
- **Shared base models** providing numeric ids, audit timestamps, and DTO metadata via `BaseEntity`/`BaseDto`.
- **JWT based authentication** (`Bearer` tokens) with pluggable secret via configuration.
- **RBAC + ABAC**: Role checks are enforced via Spring Security annotations while resource ownership checks are delegated to dedicated guards.
- **Idempotent write endpoints** using the `@Idempotent` annotation and the `idempotency_keys` table.
- **Rate limiting** built on Bucket4j with user/IP/device granularity and configurable quotas.
- **Global error contract** that always returns a deterministic payload containing `code`, `message`, `traceId`, and `timestamp`.
- **Trace propagation** via `X-Trace-Id` header coupled with MDC logging enrichment.
- **OpenAPI 3 documentation** powered by Springdoc at `/swagger-ui/index.html`.
- **ModelMapper integration** for DTO ↔ entity transformations.
- **Ready-to-use testing profile** backed by an in-memory H2 database.
- **Centralised media storage** on Amazon S3 with automatic image variants and manifest metadata.
- **Real-time notifications** via WebSocket with persistent storage and RabbitMQ-based async processing
---

## 🏗️ Microservices Architecture

The application follows a microservices architecture with independent services:

### Services

1. **Main Application** (Port 8080)
   - User management
   - Authentication & Authorization
   - Media storage
   - PostgreSQL database (port 5432)

2. **Notification Service** (Port 8081)
   - Real-time notifications via WebSocket
   - Notification persistence
   - RabbitMQ consumer for async notification processing
   - Separate PostgreSQL database (port 5433)

### Communication

- **Synchronous**: REST APIs for direct client-service communication
- **Asynchronous**: RabbitMQ for inter-service messaging
  - Exchange: `notifications.exchange`
  - Queue: `notifications.queue`
  - Routing Key: `notifications.#`

### Architecture Diagram

```
┌─────────────┐         ┌──────────────┐
│   Client    │◄────────┤ Main App     │
│             │  REST   │ (Port 8080)  │
└─────────────┘         └──────┬───────┘
                               │
                               │ RabbitMQ
                               ▼
                        ┌──────────────┐
                        │  RabbitMQ    │
                        │  (5672)      │
                        └──────┬───────┘
                               │
                               │ Consume
                               ▼
┌─────────────┐         ┌──────────────┐
│   Client    │◄────────┤ Notification │
│             │WebSocket│ Service      │
└─────────────┘         │ (Port 8081)  │
                        └──────────────┘
```

## 🌐 Developer Services

| Service                 | URL | Description            |
|-------------------------|-----|------------------------|
| **Main App Swagger**    | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) | Main API documentation |
| **Notification Service**| [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html) | Notification API docs  |
| **WebSocket Endpoint**  | [ws://localhost:8081/ws](ws://localhost:8081/ws) | Real-time notifications |
| **Redis Insight**       | [http://localhost:5540](http://localhost:5540) | Redis yönetim arayüzü  |
| **RabbitMQ Management** | [http://localhost:15672](http://localhost:15672) | Queue Management (guest/guest) |
| **Elasticsearch**       | [http://localhost:9200](http://localhost:9200) | Search & Index Service |
| **Kibana**              | [http://localhost:5601](http://localhost:5601) | Log Interface          |
| **APM Server**          | [http://localhost:8200](http://localhost:8200) | APM Server             |
| **Grafana**             | [http://localhost:3000](http://localhost:3000) |  Observation panels    |

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
├── src/main/java/com/autumnus/spring_boot_starter_template  # Main Application
│   ├── SpringBootStarterTemplateApplication.java
│   ├── common
│   │   ├── api/              # API response wrappers
│   │   ├── config/           # Configuration properties and OpenAPI settings
│   │   ├── context/          # Request context holder utilities
│   │   ├── exception/        # Error model and global exception handler
│   │   ├── idempotency/      # @Idempotent aspect and persistence layer
│   │   ├── logging/          # Trace id filter and MDC integration
│   │   ├── messaging/        # RabbitMQ producers and message DTOs
│   │   ├── persistence/      # Base JPA entities
│   │   ├── rate_limiting/    # Bucket4j backed rate limiting filter/service
│   │   └── security/         # JWT utilities, filter, and security helpers
│   └── modules
│       └── users
│           ├── controller/   # REST controller exposing versioned endpoints
│           ├── dto/          # Request/response DTOs
│           ├── entity/       # JPA entities and enums
│           ├── mapper/       # ModelMapper powered converters
│           ├── repository/   # Spring Data repositories & specifications
│           └── service/      # Service interfaces + implementations
│
└── notification-service/                                     # Notification Microservice
    ├── Dockerfile
    ├── pom.xml
    └── src/main/java/com/autumnus/notificationservice
        ├── NotificationServiceApplication.java
        ├── config/           # RabbitMQ and WebSocket configuration
        ├── controller/       # REST endpoints for notifications
        ├── dto/              # Message DTOs
        ├── entity/           # Notification entity and enums
        ├── listener/         # RabbitMQ message consumers
        ├── repository/       # JPA repositories
        └── service/          # Business logic
```

## ⚙️ Configuration

Configuration is managed through `application.yaml` and can be overridden via environment variables or profile-specific YAML files.

| Property | Description |
| --- | --- |
| `application.security.jwt-secret` | HMAC secret for signing JWT access tokens (min 32 chars). |
| `application.security.access-token-ttl` | Duration (ISO-8601) for access token lifetime. |
| `application.security.public-endpoints` | Comma-separated list of patterns that bypass authentication. |
| `application.rate-limit.capacity` | Maximum number of requests permitted per refill period. |
| `application.rate-limit.refill-period` | ISO-8601 duration describing the bucket refill cadence. |
| `application.storage.s3.bucket` | Target S3 bucket that will store static assets. |
| `application.storage.s3.region` | AWS region of the bucket (e.g. `eu-central-1`). |
| `application.storage.s3.access-key` / `secret-key` | Optional explicit credentials; falls back to default provider chain if omitted. |
| `application.storage.s3.endpoint` | Optional custom endpoint (e.g. Localstack). |
| `application.storage.s3.path-style-access` | Enable when interacting with Localstack/minio style endpoints. |
| `application.storage.s3.public-base-url` | Optional CDN/public URL prefix used when building asset links. |
| `spring.datasource.*` | Database connectivity settings (PostgreSQL by default). |
| `spring.data.redis.*` | Redis connection info for caching / distributed tokens (optional). |
| `spring.rabbitmq.*` | RabbitMQ connection settings for async messaging. |
| `application.rabbitmq.exchange` | RabbitMQ exchange name for notifications. |
| `application.rabbitmq.routing-key` | Routing key pattern for notification messages. |

> A dedicated `application-test.yaml` configures an in-memory H2 database and a deterministic JWT secret for test runs.

## 🔔 Notification Service

The notification service is an independent microservice that handles real-time notifications.

### Features

- **Real-time delivery** via WebSocket (STOMP over SockJS)
- **Persistent storage** in separate PostgreSQL database
- **Async processing** via RabbitMQ message queue
- **REST API** for querying notification history
- **Status tracking** (READ/UNREAD)
- **Type categorization** (INFO/WARNING/SUCCESS/ERROR)

### Notification Flow

1. Main application publishes notification message to RabbitMQ
2. Notification service consumes message from queue
3. Service persists notification to database
4. Service broadcasts to user via WebSocket
5. User can query notification history via REST API
6. User can mark notifications as read

### REST Endpoints

- `GET /api/v1/notifications?userId={id}` - Get user notifications (paginated)
- `PUT /api/v1/notifications/{id}/read?userId={id}` - Mark as read
- `PUT /api/v1/notifications/read-all?userId={id}` - Mark all as read
- `GET /api/v1/notifications/unread-count?userId={id}` - Get unread count

### WebSocket Connection

```javascript
// Connect to notification service
const socket = new SockJS('http://localhost:8081/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    // Subscribe to user-specific notifications
    stompClient.subscribe('/user/queue/notifications', function(message) {
        const notification = JSON.parse(message.body);
        console.log('Received:', notification);
    });
});
```

### Example: Sending Notifications

Notifications are automatically sent when certain events occur (e.g., user creation). To send custom notifications from the main application:

```java
@Autowired
private NotificationProducer notificationProducer;

public void sendCustomNotification(Long userId) {
    notificationProducer.sendNotification(
        userId,
        "Important Update",
        "Your profile has been updated successfully",
        NotificationType.SUCCESS
    );
}
```

## 📁 Media Storage

Static assets (images, audio, video, documents) are uploaded to Amazon S3 through the `MediaStorageService`. Files are organised with the following layout:

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

Images (`image/jpeg`, `image/png`) are validated to be ≤ 10 MB and automatically produce:

| Variant | Max Dimensions | Suggested Usage |
| --- | --- | --- |
| `original` | Original resolution | Archival/original downloads |
| `web` | 1920×1080 | Desktop web experiences |
| `mobile` | 1080×1080 | Handset/tablet friendly previews |
| `thumb` | 320×320 | Avatars, list thumbnails |

PDF documents are limited to 25 MB. Other media kinds can be extended with additional validation rules as needed.

To upload multiple images in a single call, use the `MediaStorageService#storeAll` API which enforces a batch size of 1–100 files and applies the same validation/variant pipeline to each item.

### User Profile & Photos

The `users` module stores the media manifest JSON directly on the `users.profile_photo_manifest` column. Each `UserResponse` now exposes the numeric `id`, audit timestamps, and a `profilePhoto` object with public URLs for every variant.

Swagger documents both administrative and self-service flows:

- `/api/v1/users/{id}` endpoints remain available for elevated roles that manage other accounts.
- `/api/v1/users/me` endpoints allow authenticated users to fetch and mutate their own profile without providing an id:
  - `GET /api/v1/users/me` — retrieve your profile.
  - `PUT /api/v1/users/me` — update your email/username.
  - `POST /api/v1/users/me/profile-photo` — upload or replace your avatar (multipart `file`, PNG/JPEG ≤ 10 MB).
  - `DELETE /api/v1/users/me/profile-photo` — remove your avatar and purge stored objects.

Once authorised in Swagger UI, select the self-service operations to update your profile—the backend extracts immutable identifiers directly from the JWT payload.

## 🔐 Security Model

- **Authentication:** Incoming requests must carry a `Bearer <token>` header containing a JWT generated with the configured secret.
- **Token payload:** Access and refresh tokens embed the immutable user id, email, username, and role claims so clients never need to submit those identifiers explicitly.
- **Authorisation:**
  - RBAC checks rely on Spring Security's `@PreAuthorize`/`@PostAuthorize` annotations (e.g. `hasRole('ADMIN')`).
  - ABAC checks leverage helper beans such as `OwnershipGuard` for owner-scoped access (`@ownershipGuard.isOwner(#id)`).
- **Context propagation:** The authenticated user id is stored in the `RequestContext` and reused by rate limiting and auditing layers.

## 🔁 Idempotency

Annotate write operations with `@Idempotent` to ensure repeatable outcomes.

```java
@PostMapping
@Idempotent
public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
    // ...
}
```

Clients must provide a unique `Idempotency-Key` header per logical request. The aspect serialises the method arguments, stores the response body + status inside the `idempotency_keys` table, and replays the cached response for identical repeats. Conflicting payloads using the same key yield a `409 CONFLICT` error with code `IDEMPOTENCY_KEY_CONFLICT`.

## 🚦 Rate Limiting

Bucket4j guards every request with a composite key built from user id, IP address, device id (`X-Device-Id` header), HTTP method, and request path. Exceeding the quota emits a `429 Too Many Requests` response with the standard error payload and a `Retry-After` header derived from configuration.

## 📚 API Documentation

Springdoc automatically exposes:

- OpenAPI spec: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui/index.html`

The documentation includes JWT bearer authentication information, enabling quick testing once a token is supplied via the UI's authorise dialog.

## 🧪 Testing

- Unit/integration tests should be executed via `./mvnw test`.
- The `test` profile spins up with an in-memory H2 database and a deterministic JWT secret.

> **Note:** The Maven wrapper downloads Maven on first run. Ensure outbound network access is available or install Maven locally if the wrapper cannot fetch the distribution.

## 🚀 Getting Started

1. **Install dependencies:** Java 17+, Docker (optional for Postgres/Redis).
2. **Configure environment:** Update `application.yaml` (or provide env vars) with real database credentials and a strong JWT secret.
3. **Run database/redis (optional):**
   ```bash
   docker compose up -d
   ```
4. **Launch the application:**
   ```bash
   ./mvnw spring-boot:run
   ```
5. **Access Swagger UI:** Open [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html).

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
