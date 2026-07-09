# ClarityHub Backend — Session Primer

Claude Code reads this file automatically. Use it to orient before touching any code.
Keep it accurate: update this file whenever the codebase state changes.

---

## What this is

Enterprise RAG platform with permission enforcement **inside** vector similarity search
(never post-retrieval). Single pgvector SQL query enforces access control before returning
any chunk to the LLM. Built on Spring Boot / Clean Architecture.

**Two repositories:**
- `clarityhub-backend` ← you are here
- `clarityhub-frontend` (React 18 + TypeScript 5 + Vite 5, not yet scaffolded)

**Three personas:** Dana (Admin), Marcus (Manager), Priya (Employee).

---

## Current phase: Phase 0 complete

Phase 0 = repository scaffold only. No domain logic exists yet. Everything below is
scaffold/plumbing; Phase 1 will add the first real use case.

---

## Repository layout

```
clarityhub-backend/
├── CLAUDE.md                          ← this file
├── pom.xml                            ← Spring Boot 3.5.3, Java 21, see key deps below
├── mvnw / mvnw.cmd                    ← Maven wrapper (use this, not system mvn)
├── .github/workflows/ci.yml          ← GitHub Actions: ./mvnw --batch-mode verify
├── docs/
│   ├── engineering-standards.md      ← non-negotiable rules for every PR
│   ├── requirements/
│   │   └── 00-phase0-specification.md ← Phase 0 design doc + links to GitHub Issues
│   └── architecture-decisions.md     ← ADR index (ADR-001 through ADR-011)
├── infra/
│   ├── docker-compose.yml            ← pgvector/pgvector:pg16, env vars from .env
│   └── .env.example                  ← DB_NAME, DB_USERNAME, DB_PASSWORD, DB_URL
└── src/
    ├── main/
    │   ├── java/com/clarityhub/
    │   │   ├── ClarityHubApplication.java          ← @SpringBootApplication entry point
    │   │   ├── domain/package-info.java            ← layer boundary marker (no deps)
    │   │   ├── application/
    │   │   │   ├── port/in/package-info.java       ← inbound ports (interfaces)
    │   │   │   ├── port/out/package-info.java      ← outbound ports (interfaces)
    │   │   │   └── usecase/package-info.java       ← use case implementations
    │   │   ├── infrastructure/package-info.java    ← adapters, JPA repos, external
    │   │   └── presentation/package-info.java      ← REST controllers, DTOs
    │   └── resources/
    │       ├── application.yml                     ← base config (ddl-auto=validate)
    │       ├── application-local.yml               ← reads DB_URL/USER/PASS from env
    │       ├── application-prod.yml                ← disables OpenAPI docs
    │       └── db/migration/
    │           └── V1__enable_vector_extension.sql ← CREATE EXTENSION IF NOT EXISTS vector
    └── test/
        ├── java/com/clarityhub/
        │   ├── TestcontainersConfiguration.java    ← @ServiceConnection postgres bean
        │   ├── architecture/ArchitectureTest.java  ← 4 ArchUnit layer dependency tests
        │   ├── migration/FlywayMigrationTest.java  ← verifies V1 ran + vector extension
        │   └── openapi/
        │       ├── OpenApiAvailableTest.java       ← GET /v3/api-docs → 200 in non-prod
        │       └── OpenApiDisabledTest.java        ← GET /v3/api-docs → 404 with prod profile
        └── resources/application-test.yml         ← test profile config
```

---

## Key dependencies (pom.xml)

| Artifact | Version | Scope |
|---|---|---|
| spring-boot-starter-web | 3.5.3 | compile |
| spring-boot-starter-data-jpa | 3.5.3 | compile |
| postgresql | (BOM) | runtime |
| flyway-core + flyway-database-postgresql | (BOM) | compile |
| springdoc-openapi-starter-webmvc-ui | 2.8.8 | compile |
| spring-boot-starter-actuator | 3.5.3 | compile |
| spring-boot-starter-test | 3.5.3 | test |
| spring-boot-testcontainers | 3.5.3 | test |
| archunit-junit5 | 1.4.0 | test |
| testcontainers:junit-jupiter | 1.21.0 | test |
| testcontainers:postgresql | 1.21.0 | test |

---

## Architecture rules (enforced by ArchUnit)

Dependency direction: domain ← application ← infrastructure / presentation

| Rule | Detail |
|---|---|
| domain → nothing | No deps on application, infrastructure, presentation |
| domain no framework | No Spring, no Jakarta Persistence annotations |
| application → no infra | application layer cannot import infrastructure or presentation |
| infrastructure → no presentation | infrastructure cannot import presentation |

Violations fail the build. Add tests in `ArchitectureTest.java`.

---

## Critical engineering rules (from docs/engineering-standards.md)

- **Permission enforcement:** must happen **inside** the vector query SQL predicate, never as a post-retrieval filter. Adversarial tests are mandatory for every access-control path.
- **Flyway migrations:** append-only. Never edit an existing migration file. `ddl-auto=validate` everywhere — Hibernate never touches the schema.
- **No secrets in source:** all credentials via environment variables. `.env` is gitignored.
- **Audit log:** INSERT-only at DB privilege level; no UPDATE/DELETE on audit tables.
- **API paths:** `/api/v1/` prefix. Return HTTP 404 (not 403) for access-controlled resources a user cannot see.
- **OpenAPI:** enabled in all non-prod profiles; disabled in `prod` profile.

---

## Test coverage thresholds (enforced from Phase 1 onward)

| Layer | Threshold |
|---|---|
| domain | 90% |
| application | 85% |
| infrastructure | 70% |
| presentation | 70% |

JaCoCo `check` goal is wired but has no thresholds yet — add them when the first use case lands.

---

## Running locally

```bash
# Start database (requires infra/.env — copy from infra/.env.example)
docker compose -f infra/docker-compose.yml up -d

# Run tests (Docker Desktop must be running)
./mvnw test

# Full build + verify (includes JaCoCo report)
./mvnw verify

# Run application (requires local profile)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

---

## Docker Desktop 29.x compatibility (macOS)

Testcontainers 1.21 / docker-java 3.4 defaults to Docker API v1.32 in HTTP URL paths.
Docker Desktop 29.x requires minimum v1.44.

**Fix in pom.xml (maven-surefire-plugin):**
- `<api.version>1.44</api.version>` as a JVM system property — this is the only path that
  reaches `overrideDockerPropertiesWithSystemProperties()`. The env var `DOCKER_API_VERSION`
  does **not** work because `overrideDockerPropertiesWithEnv()` only matches keys that
  literally appear in `CONFIG_KEYS` (which contains `api.version`, not `DOCKER_API_VERSION`).
- `TESTCONTAINERS_RYUK_DISABLED=true` — Ryuk (resource reaper) hits the same API version
  issue before the postgres container even starts. Disabled; Docker Desktop cleans up.

**Do not change these settings** without testing against Docker Desktop 29.x locally.

---

## What does not exist yet (Phase 1+)

- No domain entities, value objects, or aggregates
- No use cases or port interfaces
- No JPA repositories or infrastructure adapters
- No REST controllers or DTOs
- No JWT authentication / security config
- No pgvector column or embedding logic
- No LLM integration (Claude adapter stub)
- No frontend (`clarityhub-frontend` repo not yet scaffolded)

---

## GitHub Issues (backend)

Tracked at `getClarityHub/clarityhub-backend`. Phase 0 issues are closed.
Phase 1 issues will be created before implementation begins — check the issue tracker
before starting any new feature work.
