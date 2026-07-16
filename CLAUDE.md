# ClarityHub Backend — Session Primer

Claude Code reads this file automatically. Use it to orient before touching any code.
Keep it accurate: update this file whenever the codebase state changes. Don't commit or push anything before asking for permission.

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
├── pom.xml                            ← Spring Boot 3.5.3, Java 21, see key deps + plugins below
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
    │       ├── application.yml                     ← base config (datasource, JPA, actuator, springdoc)
    │       ├── application-local.yml               ← dev overrides (log levels)
    │       ├── application-prod.yml                ← prod overrides (springdoc off, WARN root)
    │       └── db/migration/
    │           └── V1__enable_vector_extension.sql ← CREATE EXTENSION IF NOT EXISTS vector
    └── test/
        ├── java/com/clarityhub/
        │   ├── TestcontainersConfiguration.java    ← @ServiceConnection postgres bean
        │   ├── architecture/ArchitectureTest.java  ← 7 ArchUnit rules (deps + annotations)
        │   ├── migration/FlywayMigrationTest.java  ← verifies V1 ran + vector extension
        │   └── openapi/
        │       ├── OpenApiAvailableTest.java       ← GET /v3/api-docs → 200 in non-prod
        │       └── OpenApiDisabledTest.java        ← 404 AND OpenAPI bean absent under prod profile
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
| spring-boot-starter-actuator | 3.5.3 | compile (health only in Phase 0) |
| spring-boot-starter-test | 3.5.3 | test |
| spring-boot-testcontainers | 3.5.3 | test |
| archunit-junit5 | 1.4.0 | test |
| testcontainers:junit-jupiter | 1.21.0 | test |
| testcontainers:postgresql | 1.21.0 | test |

---

## Build plugins (pom.xml)

| Plugin | Purpose |
|---|---|
| spring-boot-maven-plugin | Fat-jar / repackage |
| maven-enforcer-plugin (3.5.0) | Fails build on Java < 21, Maven < 3.9, dependency convergence issues, or SNAPSHOT deps in release |
| spotless-maven-plugin (2.44.3) | Enforces google-java-format AOSP style + pom.xml sort. `verify` runs `check`; run `./mvnw spotless:apply` to auto-format |
| maven-surefire-plugin | Configures Testcontainers Docker API workaround (see below) |
| jacoco-maven-plugin (0.8.13) | `prepare-agent` + `report` + `check` all bound to `verify`. Threshold placeholder (0%) proves wiring; raise per-layer in Phase 1 |

Run all quality gates locally with `./mvnw --batch-mode verify`.

---

## Architecture rules (enforced by ArchUnit)

Dependency direction: domain ← application ← infrastructure / presentation

| Rule | Detail |
|---|---|
| domain → nothing | No deps on application, infrastructure, presentation |
| domain no framework | No Spring, no Jakarta Persistence classes on classpath |
| domain no framework annotations | @Component/@Service/@Repository/@Controller/@Configuration/@Entity/@Embeddable/@MappedSuperclass forbidden |
| application → no infra | application layer cannot import infrastructure or presentation |
| infrastructure → no presentation | infrastructure cannot import presentation |
| @Entity only in infrastructure | forward guard for Phase 1 (`allowEmptyShould(true)` in Phase 0) |
| @RestController/@Controller only in presentation | forward guard for Phase 1 (`allowEmptyShould(true)` in Phase 0) |

Violations fail the build. Add new rules in `ArchitectureTest.java` — favor `allowEmptyShould(true)` for forward-looking guards until real classes exist.

---

## Critical engineering rules (from docs/engineering-standards.md)

- **Permission enforcement:** must happen **inside** the vector query SQL predicate, never as a post-retrieval filter. Adversarial tests are mandatory for every access-control path.
- **Flyway migrations:** append-only. Never edit an existing migration file. `ddl-auto=validate` everywhere — Hibernate never touches the schema.
- **No secrets in source:** all credentials via environment variables. `.env` is gitignored.
- **Audit log:** INSERT-only at DB privilege level; no UPDATE/DELETE on audit tables.
- **API paths:** `/api/v1/` prefix. Return HTTP 404 (not 403) for access-controlled resources a user cannot see.
- **OpenAPI:** enabled in all non-prod profiles; disabled in `prod` profile. Verified by `OpenApiDisabledTest` which asserts *both* the 404 and the absence of the `io.swagger.v3.oas.models.OpenAPI` bean (test the cause, not just the symptom).

---

## Senior-developer code standards

Follow these when writing or reviewing any code in this repo. They exist to keep the codebase legible, safe, and cheap to evolve.

### Design
- **Clean Architecture is load-bearing, not aspirational.** Domain must stay framework-free. If you need a Spring feature in domain, you're modeling wrong — introduce a port in `application/port/out` and implement it in `infrastructure`.
- **Constructor injection only.** No field injection (`@Autowired` on fields). Constructor injection makes dependencies explicit and enables true unit tests without Spring context.
- **Prefer records for immutable data** (DTOs, value objects, query results). Reserve classes for entities with identity/behavior.
- **Value objects over primitives** for domain concepts (e.g., `DocumentId`, `TenantId` — not `UUID` or `String` passed around).
- **Package-by-feature inside each layer.** Once a layer has more than ~5 classes, group by capability (e.g., `application/usecase/upload/`, not `application/usecase/UploadCommand.java` alongside 20 others).

### Testing
- **Test behavior, not implementation.** Don't verify mock interactions when a state-based assertion works.
- **Test the cause, not the symptom.** A 404 can happen for many reasons — assert the specific mechanism (bean absent, config disabled) when you can.
- **Testcontainers over mocks for anything that touches SQL.** Mocked JDBC gives false confidence; a real pgvector container catches migration and dialect bugs.
- **Adversarial tests are mandatory for every permission-controlled query.** Write the "unauthorized user tries to retrieve someone else's chunk" test *before* the happy path.
- **Coverage thresholds per layer** (from Phase 1): domain 90%, application 85%, infrastructure 70%, presentation 70%. JaCoCo `check` is wired now with a 0% placeholder — raise it when real code lands.

### Code style
- **Spotless is the arbiter.** Don't argue about formatting in review. Run `./mvnw spotless:apply` before opening a PR.
- **No comments describing what the code does.** Names should carry that. Comments explain *why* — hidden constraints, workarounds, invariants that aren't obvious.
- **Don't write JavaDoc on obvious things** (getters, single-line delegators). Reserve JavaDoc for public API and package-info.
- **Guard clauses over nested ifs.** Fail fast at the top of a method.
- **Optional at boundaries, not fields.** Return `Optional<T>` from repositories; never store `Optional` as a field.
- **No `null` returned from collection-returning methods.** Return empty collections.

### Errors and boundaries
- **Validate at system boundaries only** (controllers, message consumers, external API responses). Internal code trusts its callers.
- **No catch-all `catch (Exception e)`** unless you rethrow after context enrichment or the caller genuinely cannot handle a narrower type.
- **Never log-and-swallow.** Either handle the error meaningfully or propagate it.
- **404, not 403, for access-controlled resources** the caller isn't allowed to see. Existence itself is information.

### Persistence
- **Migrations are append-only.** New file per change — never modify a shipped migration.
- **`ddl-auto=validate` everywhere.** Any dev who bumps it to `update` for convenience is asking for a prod incident.
- **JPA `@Entity` classes belong in `infrastructure`.** They are persistence adapters, not the domain model. Map them to/from domain objects at the repository boundary.
- **No JPA lazy loading across service boundaries.** `open-in-view: false` is set intentionally — never re-enable it.

### Dependencies
- **Every dependency is a promise to maintain something.** Justify additions in the PR description.
- **Pin exact versions** for reproducibility. Review patch bumps monthly (security fixes ship as patches).
- **Enforcer catches conflicts early.** If `dependencyConvergence` fails, fix the root cause (explicit `<dependencyManagement>` entry) rather than suppressing the rule.

### Configuration
- **Env vars for anything environment-specific** — never hardcode URLs, credentials, or tuning parameters.
- **`application.yml` is the single source.** Profile files (`-local`, `-prod`) override only what genuinely differs — resist duplication.
- **Secrets never appear in git history.** If one leaks, rotate immediately; don't just delete the file.

### Reviews
- **Small PRs.** If the diff exceeds ~400 lines of production code, split it.
- **Every PR passes `./mvnw verify` locally before pushing.** CI is a backup, not a first line of defense.

---

## Test coverage thresholds (enforced from Phase 1 onward)

| Layer | Threshold |
|---|---|
| domain | 90% |
| application | 85% |
| infrastructure | 70% |
| presentation | 70% |

JaCoCo `check` goal is wired with a placeholder 0% threshold at BUNDLE level — proves the plugin is functional. Replace with per-layer rules when the first use case lands.

---

## Running locally

```bash
# Start database (requires infra/.env — copy from infra/.env.example)
docker compose -f infra/docker-compose.yml up -d

# Auto-format code (do this before every commit)
./mvnw spotless:apply

# Run tests (Docker Desktop must be running)
./mvnw test

# Full build + all quality gates (enforcer, tests, spotless:check, jacoco:check)
./mvnw verify

# Run application (requires local profile)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

`./mvnw verify` is the definition of "ready to push." If it's green locally, it's green in CI.

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
