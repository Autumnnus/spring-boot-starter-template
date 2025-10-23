# Spring Boot Starter Template

This project provides an opinionated Spring Boot starter template that implements production-ready defaults for building modular, multi-tenant ready REST APIs. It follows the API design rules that emphasise deterministic responses, idempotent writes, RBAC/ABAC authorisation, centralised error handling, and observability-friendly logging.

## ✨ Highlights

- **Modular architecture** with domain-specific modules under `modules/` (e.g. `users`) and cross-cutting concerns in `common/`.
- **JWT based authentication** (`Bearer` tokens) with pluggable secret via configuration.
- **RBAC + ABAC**: Role checks are enforced via Spring Security annotations while resource ownership checks are delegated to dedicated guards.
- **Idempotent write endpoints** using the `@Idempotent` annotation and the `idempotency_keys` table.
- **Rate limiting** built on Bucket4j with user/IP/device granularity and configurable quotas.
- **Global error contract** that always returns a deterministic payload containing `code`, `message`, `traceId`, and `timestamp`.
- **Trace propagation** via `X-Trace-Id` header coupled with MDC logging enrichment.
- **OpenAPI 3 documentation** powered by Springdoc at `/swagger-ui/index.html`.
- **ModelMapper integration** for DTO ↔ entity transformations.
- **Ready-to-use testing profile** backed by an in-memory H2 database.

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

## ⚙️ Configuration

Configuration is managed through `application.yaml` and can be overridden via environment variables or profile-specific YAML files.

| Property | Description |
| --- | --- |
| `application.security.jwt-secret` | HMAC secret for signing JWT access tokens (min 32 chars). |
| `application.security.access-token-ttl` | Duration (ISO-8601) for access token lifetime. |
| `application.security.public-endpoints` | Comma-separated list of patterns that bypass authentication. |
| `application.rate-limit.capacity` | Maximum number of requests permitted per refill period. |
| `application.rate-limit.refill-period` | ISO-8601 duration describing the bucket refill cadence. |
| `spring.datasource.*` | Database connectivity settings (PostgreSQL by default). |
| `spring.data.redis.*` | Redis connection info for caching / distributed tokens (optional). |

> A dedicated `application-test.yaml` configures an in-memory H2 database and a deterministic JWT secret for test runs.

## 🔐 Security Model

- **Authentication:** Incoming requests must carry a `Bearer <token>` header containing a JWT generated with the configured secret.
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
