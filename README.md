# Spring Boot Starter Template

This project provides an opinionated Spring Boot starter template that implements production-ready defaults for building
modular, multi-tenant ready REST APIs. It follows the API design rules that emphasise deterministic responses,
idempotent writes, RBAC/ABAC authorisation, centralised error handling, and observability-friendly logging.

## ✨ Highlights

- **Modular architecture** with domain-specific modules under `modules/` (e.g. `users`) and cross-cutting concerns in
  `common/`.
- **Shared base models** providing numeric ids, audit timestamps, and DTO metadata via `BaseEntity`/`BaseDto`.
- **JWT based authentication** (`Bearer` tokens) with pluggable secret via configuration.
- **OAuth2 integration** with Google (and extensible to other providers) for social login.
- **Email service** with beautiful HTML templates for verification, password reset, and notifications.
- **RBAC + ABAC**: Role checks are enforced via Spring Security annotations while resource ownership checks are
  delegated to dedicated guards.
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
- **Internationalization (i18n)** with full multi-language support for error messages, validation messages, and notifications (Turkish & English).

---

## 🌐 Geliştirici Servisleri

| Service                          | URL                                                                                        | Description                   |
|----------------------------------|--------------------------------------------------------------------------------------------|-------------------------------|
| **Swagger (Gateway)**            | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) | API dokümantasyonu            |
| **OAuth2 Test Page**             | [http://localhost:8080/oauth-test.html](http://localhost:8080/oauth-test.html)             | OAuth2 & Auth Testing         |
| **Redis Insight**                | [http://localhost:5540](http://localhost:5540)                                             | Redis yönetim arayüzü         |
| **RabbitMQ Management**          | [http://localhost:15672](http://localhost:15672)                                           | Queue Management              |
| **Notification Service Swagger** | [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html) | Notification microservice API |
| **Elasticsearch**                | [http://localhost:9200](http://localhost:9200)                                             | Search & Index Service        |
| **Kibana**                       | [http://localhost:5601](http://localhost:5601)                                             | Log Interface                 |
| **APM Server**                   | [http://localhost:8200](http://localhost:8200)                                             | APM Server                    |
| **Grafana**                      | [http://localhost:3000](http://localhost:3000)                                             | Observation panels            |

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
│   ├── i18n/             # Internationalization configuration and MessageService
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
| `application.security.refresh-token-ttl`           | Duration (ISO-8601) for refresh token lifetime (default: 7 days).               |
| `application.security.email-verification-token-ttl`| Duration (ISO-8601) for email verification token (default: 24h).                |
| `application.security.password-reset-token-ttl`    | Duration (ISO-8601) for password reset token (default: 1h).                     |
| `application.security.public-endpoints`            | Comma-separated list of patterns that bypass authentication.                    |
| `application.security.oauth2.success-redirect-url` | Frontend URL to redirect after successful OAuth2 login.                         |
| `application.security.oauth2.failure-redirect-url` | Frontend URL to redirect after failed OAuth2 login.                             |
| `application.email.from`                           | Email address used as sender for all emails.                                    |
| `application.email.from-name`                      | Display name for email sender.                                                  |
| `application.email.base-url`                       | Base URL for generating email verification and reset links.                     |
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
| `spring.security.oauth2.client.registration.google.client-id` | Google OAuth2 client ID from Google Cloud Console.             |
| `spring.security.oauth2.client.registration.google.client-secret` | Google OAuth2 client secret from Google Cloud Console.       |
| `spring.mail.host`                                 | SMTP server host (e.g. smtp.gmail.com).                                         |
| `spring.mail.port`                                 | SMTP server port (default: 587 for TLS).                                        |
| `spring.mail.username`                             | SMTP username (email address for Gmail).                                        |
| `spring.mail.password`                             | SMTP password (app password for Gmail with 2FA).                                |

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

## 🔐 Authentication & Authorization

### Traditional Authentication

The system provides a complete authentication flow with:

- **User Registration** - Email-based registration with verification
- **Login** - JWT token-based authentication (access + refresh tokens)
- **Email Verification** - Secure email verification flow
- **Password Reset** - Token-based password reset via email
- **Password Change** - Authenticated password change with token revocation

### OAuth2 Social Login

Integrated OAuth2 support for social authentication:

- **Google OAuth2** - Login with Google account
- **Automatic User Creation** - Creates users on first OAuth2 login
- **Profile Sync** - Syncs profile picture from OAuth2 provider
- **Email Verification** - OAuth2 users are auto-verified

**Test OAuth2:** Visit [http://localhost:8080/oauth-test.html](http://localhost:8080/oauth-test.html)

### Email Service

Automated email notifications with beautiful HTML templates:

- **Verification Emails** - Sent after registration (24h expiry)
- **Welcome Emails** - Sent after successful email verification
- **Password Reset** - Secure password reset links (1h expiry)
- **Password Changed** - Security notifications for password changes

All emails are sent asynchronously and use professional responsive HTML templates.

**Detailed Setup:** See [AUTH_SETUP.md](AUTH_SETUP.md) for OAuth2 and email configuration.

### Quick Test

1. **OAuth2 Test Page:** [http://localhost:8080/oauth-test.html](http://localhost:8080/oauth-test.html)
2. **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
3. **Traditional Login:**
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"user@example.com","password":"password"}'
   ```

### Authentication Endpoints

| Endpoint                           | Method | Description                    |
|------------------------------------|--------|--------------------------------|
| `/api/v1/auth/register`            | POST   | Register new user              |
| `/api/v1/auth/login`               | POST   | Login with credentials         |
| `/api/v1/auth/verify-email`        | GET    | Verify email with token        |
| `/api/v1/auth/request-password-reset` | POST   | Request password reset         |
| `/api/v1/auth/reset-password`      | POST   | Reset password with token      |
| `/api/v1/auth/change-password`     | POST   | Change password (authenticated)|
| `/api/v1/auth/refresh`             | POST   | Refresh access token           |
| `/api/v1/auth/logout`              | POST   | Logout and revoke token        |
| `/oauth2/authorization/google`     | GET    | Initiate Google OAuth2 login   |

## 🌍 Internationalization (i18n)

The application provides full multi-language support for all user-facing messages including error messages, validation messages, and notifications. Currently supported languages are **English (en)** and **Turkish (tr)**.

### How it Works

- **Language Detection:** The application automatically detects the user's preferred language from the `Accept-Language` HTTP header.
- **Default Language:** English is used as the default fallback language.
- **Message Resolution:** All messages are stored in property files under `src/main/resources/i18n/`:
  - `messages_en.properties` / `messages_tr.properties` - General messages
  - `validation_en.properties` / `validation_tr.properties` - Validation messages

### Using MessageService

The `MessageService` provides convenient methods for retrieving localized messages:

```java
@Service
@RequiredArgsConstructor
public class MyService {
    private final MessageService messageService;

    public void doSomething() {
        // Get message for current locale (from Accept-Language header)
        String message = messageService.getMessage("user.created");

        // Get message with parameters
        String welcome = messageService.getMessage("email.welcome.subject", "MyApp");

        // Get message for specific locale
        String trMessage = messageService.getMessage("user.created", new Locale("tr"));

        // Get message with default fallback
        String custom = messageService.getMessageOrDefault("custom.key", "Default message");
    }
}
```

### Testing i18n

Test endpoints are available at `/api/v1/i18n` to verify language support:

```bash
# Test English (default)
curl -X GET http://localhost:8080/api/v1/i18n/test \
  -H "Accept-Language: en"

# Test Turkish
curl -X GET http://localhost:8080/api/v1/i18n/test \
  -H "Accept-Language: tr"

# Test all locales
curl -X GET http://localhost:8080/api/v1/i18n/test-all-locales

# Test error message localization
curl -X GET http://localhost:8080/api/v1/i18n/test-error \
  -H "Accept-Language: tr"
```

### Adding New Languages

To add support for a new language:

1. Create new message files: `messages_{locale}.properties` and `validation_{locale}.properties`
2. Copy the content from English files and translate all values
3. The system will automatically detect and use the new language based on the `Accept-Language` header

Example for German (de):
```properties
# messages_de.properties
app.welcome=Willkommen bei Spring Boot Starter Template
user.created=Benutzer erfolgreich erstellt
# ... etc
```

### Message Keys Reference

All available message keys can be found in:
- `src/main/resources/i18n/messages_en.properties` - Application messages
- `src/main/resources/i18n/validation_en.properties` - Validation messages

Key categories include:
- `error.*` - Error messages
- `auth.*` - Authentication & authorization messages
- `user.*` - User management messages
- `role.*` / `permission.*` - RBAC messages
- `media.*` - Media storage messages
- `notification.*` - Notification messages
- `validation.*` - Validation messages

## 🔐 Security Model

- **Authentication:** Incoming requests must carry a `Bearer <token>` header containing a JWT generated with the
  configured secret.
- **Token payload:** Access and refresh tokens embed the immutable user id, email, username, and role claims so clients
  never need to submit those identifiers explicitly.
- **Authorisation:**
    - RBAC checks rely on Spring Security's `@PreAuthorize`/`@PostAuthorize` annotations (e.g. `hasRole('ADMIN')`).
    - ABAC checks leverage helper beans such as `OwnershipGuard` for owner-scoped access (
      `@ownershipGuard.isOwner(#id)`).
- **Context propagation:** The authenticated user id is stored in the `RequestContext` and reused by rate limiting and
  auditing layers.

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

## 🚀 Getting Started

1. **Install dependencies:** Java 17+, Docker (for Postgres/Redis/RabbitMQ/Elasticsearch).
2. **Configure environment:**
   - Copy `.env.example` to `.env`
   - Update database credentials, JWT secret (min 32 chars)
   - Configure OAuth2 (see [AUTH_SETUP.md](AUTH_SETUP.md))
   - Configure email service (SMTP settings)
3. **Run infrastructure services:**
   ```bash
   docker compose up -d
   ```
4. **Launch the application:**
   ```bash
   ./mvnw spring-boot:run
   ```
5. **Test the system:**
   - **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
   - **OAuth2 Test:** [http://localhost:8080/oauth-test.html](http://localhost:8080/oauth-test.html)

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
- Spring Security (JWT + OAuth2)
- Spring Data JPA
- Spring Mail (JavaMailSender)
- Thymeleaf (email templates)
- Spring i18n (internationalization)
- Bucket4j (rate limiting)
- ModelMapper (DTO mapping)
- JJWT (JWT signing)
- Springdoc OpenAPI
- Lombok
- PostgreSQL, Redis, RabbitMQ, Elasticsearch

## 📄 License

This starter is provided as-is. Adapt, extend, and integrate it into your own services as needed.
