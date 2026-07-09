# Engineering Standards

**Status:** Active  
**Date:** 2026-07-02  
**Author:** christofferka  

These standards apply to every line of code, every migration, and every pipeline change in ClarityHub. They are the operational form of the non-negotiable rules in `architecture-decisions.md`. When a standard conflicts with convenience, the standard wins. When a standard conflicts with a security requirement, flag it rather than choosing either unilaterally.

---

## Technology stack

| Layer | Technology | Version |
|---|---|---|
| Backend language | Java | 21 (LTS) |
| Backend framework | Spring Boot | 3.5.x (latest patch) |
| Build tool | Maven | 3.9.x |
| Database | PostgreSQL | 16 |
| Vector extension | pgvector | 0.7.x |
| Migrations | Flyway | 10.x |
| API documentation | SpringDoc OpenAPI | 2.x |
| Frontend language | TypeScript | 5.x (strict mode) |
| Frontend framework | React | 18.x |
| Frontend build | Vite | 5.x |
| Containerisation | Docker + Docker Compose | latest stable |
| CI | GitHub Actions | — |

---

## Package structure

Every Java class lives under `com.clarityhub` in exactly one of these four layers. No class may exist outside these packages.

```
com.clarityhub.domain          # Entities, value objects, domain events. No framework imports.
com.clarityhub.application     # Use cases, port interfaces.
  .port.in                     # Inbound ports (interfaces the presentation layer calls).
  .port.out                    # Outbound ports (interfaces the infrastructure layer implements).
  .usecase                     # Use case implementations.
com.clarityhub.infrastructure  # Adapter implementations: JPA, pgvector, LLM, storage, email.
com.clarityhub.presentation    # REST controllers, request/response DTOs, exception handlers.
```

**Dependency rule (enforced, not aspirational):** imports flow inward only. `domain` imports nothing from the other three. `application` imports `domain` only. `infrastructure` and `presentation` import `application` and `domain`. Neither `infrastructure` nor `presentation` imports the other.

Violations of the dependency rule are caught by ArchUnit tests that run in CI. A failing ArchUnit test is a build failure.

---

## Secret handling

- Secrets are never in source code, configuration files, or container images.
- Secrets are never committed to git under any circumstances. A committed secret requires immediate rotation before any other work continues.
- Local development: secrets are injected as environment variables. The `.env` file pattern is acceptable locally if and only if `.env` is in `.gitignore` and developers are informed it must never be committed.
- CI: secrets come from GitHub Actions secrets, not from checked-in files.
- Production: secrets come from Azure Key Vault, injected as environment variables at container startup.
- Database credentials, JWT signing secrets, the Anthropic API key, and any future third-party API keys are all governed by this rule without exception.

---

## Database migrations

- All schema changes are made through Flyway migrations, never through Hibernate `ddl-auto` in any environment including local development. Set `spring.jpa.hibernate.ddl-auto=validate` everywhere.
- Migration files are named `V{version}__{description}.sql` using sequential integers starting at 1.
- Migrations are append-only. An applied migration is never edited. If a migration was wrong, write a corrective migration.
- Every migration runs in a transaction unless the SQL explicitly cannot (e.g., `CREATE INDEX CONCURRENTLY`). Non-transactional migrations must be annotated in a comment.
- The first migration enables the vector extension: `CREATE EXTENSION IF NOT EXISTS vector;`

---

## Audit log

- The audit log table is append-only, enforced at the database privilege level. The application user has INSERT privilege only on the audit table. No UPDATE or DELETE.
- Every security-relevant action is logged: login attempts (success and failure), document upload, document deletion, permission changes, every AI query with the user ID, the chunks retrieved, and the similarity scores.
- Audit log entries are never modified or deleted by application code. Retention is an infrastructure policy.

---

## Permission enforcement

- Access control is enforced inside the vector similarity search query. It is never a post-retrieval filter.
- Any code path that retrieves chunks before applying permission filters is a bug, not a feature gap. Treat it as a security incident.
- The existence of a restricted document must not be revealed to a user without access. Error messages, empty results, and citations must all be indistinguishable from the response to a query that simply has no relevant documents.

---

## Test strategy and coverage

### Coverage thresholds (enforced by JaCoCo in CI)

| Layer | Line coverage minimum |
|---|---|
| `domain` | 90% |
| `application` | 85% |
| `infrastructure` | 70% |
| `presentation` | 70% |

CI fails if any threshold is not met.

### Test categories

**Unit tests** (`src/test/java`, no Spring context, no database)  
- Domain logic and value object invariants.  
- Use case logic with all outbound ports mocked.  
- Fast: the full unit suite runs in under 30 seconds.

**Integration tests** (`src/test/java`, `@SpringBootTest` or slice tests with Testcontainers)  
- Database queries including permission-scoped vector search, verified against a real PostgreSQL + pgvector container.  
- REST endpoints via `MockMvc` or `WebTestClient`.  
- Flyway migrations verified to apply cleanly on a fresh schema.

**Adversarial tests** (integration tests, clearly marked `@Tag("security")`)  
- Mandatory for any feature touching retrieval, search, or chat.  
- Prove that a lower-privileged user cannot extract content from a document they are not authorised to read, through any path: direct query, search, chat, citation, error message, or inference from partial content.  
- A failing adversarial test is a release blocker. It is not scheduled as a bug.

**Architecture tests** (ArchUnit, run as unit tests)  
- Enforce the dependency rule: no inward-violation imports.  
- Enforce that the `domain` layer imports nothing from `org.springframework`, `jakarta.persistence`, or any infrastructure package.

---

## Commit conventions

Format: `type(scope): subject`

```
feat(auth): add JWT refresh rotation
fix(retrieval): apply permission filter before vector search
test(retrieval): adversarial test for cross-permission extraction
chore(ci): add JaCoCo coverage gate
docs(adr): add ADR-012 for embedding model choice
```

Types: `feat`, `fix`, `test`, `refactor`, `chore`, `docs`, `ci`  
Subject: lowercase, present tense, no trailing period, under 72 characters.  
Body: required for anything non-obvious. Explain why, not what.

---

## CI requirements

Every push to any branch runs:

1. `mvn verify` — compiles, runs all tests, enforces JaCoCo thresholds, runs ArchUnit.
2. Frontend `tsc --noEmit` — TypeScript strict type check.
3. Frontend `npm test` — unit tests.

A pull request may not be merged if CI is red. No exceptions.

The CI pipeline must not contain secrets. All credentials are injected from GitHub Actions secrets.

---

## New document defaults

When a document is uploaded, its access is set to the most restrictive available policy by default. It is never accessible to any user other than the uploader until a manager explicitly grants access. This rule is enforced in the domain layer, not the presentation layer.

---

## API design

- REST. JSON.
- Versioned under `/api/v1/`.
- Authentication via JWT Bearer token on all endpoints except `/api/v1/auth/login` and `/api/v1/auth/refresh`.
- HTTP 404 is returned for any resource the authenticated user does not have access to, indistinguishable from a resource that does not exist. HTTP 403 is never used for access-controlled content because it confirms existence.
- OpenAPI documentation is auto-generated via SpringDoc and available at `/swagger-ui.html` in development. It is not exposed in production.

---

## Frontend standards

- TypeScript strict mode is required. `"strict": true` in `tsconfig.json`. The build fails if it is not set.
- No `any`. Lint rule enforced.
- Component files: PascalCase (`DocumentList.tsx`).
- Utility and hook files: camelCase (`useDocuments.ts`).
- All API calls go through a typed client layer. No raw `fetch` calls in components.

---

## Local development prerequisites

A developer must be able to run the full local stack with two commands:

```bash
docker compose up -d        # starts PostgreSQL + pgvector
./mvnw spring-boot:run      # starts the backend, Flyway runs migrations on startup
```

No manual database setup. No manual secret injection beyond a `.env` file documented in `README.md`. If it takes more than two commands, the setup is broken.