# Phase 0 — Foundation Design Document

**Status:** Active  
**Date:** 2026-07-02  
**Author:** christofferka

---

## Goal

Phase 0 produces no user-facing functionality. Its output is a repository pair where every subsequent phase can be built correctly from day one: right structure, right boundaries, right pipeline. A Phase 0 that is merely "good enough to start" is a failure — shortcuts baked in here will need unpicking under time pressure later.

---

## Repository structure

Two repositories:

| Repository | Contents |
|---|---|
| `getClarityHub/clarityhub-backend` | Spring Boot application at root, `infra/` (Docker Compose), `docs/` |
| `getClarityHub/clarityhub-frontend` | Vite + React + TypeScript application at root |

---

## Tasks

### Backend — `getClarityHub/clarityhub-backend`

| Issue | Task |
|---|---|
| [#8](https://github.com/getClarityHub/clarityhub-backend/issues/8) | Repository `.gitignore` and directory structure |
| [#2](https://github.com/getClarityHub/clarityhub-backend/issues/2) | Spring Boot backend scaffold |
| [#3](https://github.com/getClarityHub/clarityhub-backend/issues/3) | Local database with Docker Compose |
| [#4](https://github.com/getClarityHub/clarityhub-backend/issues/4) | Flyway migrations |
| [#6](https://github.com/getClarityHub/clarityhub-backend/issues/6) | GitHub Actions CI pipeline |
| [#7](https://github.com/getClarityHub/clarityhub-backend/issues/7) | OpenAPI documentation |

### Frontend — `getClarityHub/clarityhub-frontend`

| Issue | Task |
|---|---|
| [#1](https://github.com/getClarityHub/clarityhub-frontend/issues/1) | Repository `.gitignore` and directory structure |
| [#2](https://github.com/getClarityHub/clarityhub-frontend/issues/2) | React + TypeScript frontend scaffold |
| [#3](https://github.com/getClarityHub/clarityhub-frontend/issues/3) | GitHub Actions CI pipeline |

---

## Key decisions

**ArchUnit from day one.** The four-layer dependency rule (ADR-002) is enforced by an ArchUnit test that runs in CI. Added at scaffold time so violations cannot accumulate before it is introduced.

**Flyway owns the schema, always.** `spring.jpa.hibernate.ddl-auto=validate` in every Spring profile without exception.

**Testcontainers in CI.** Backend integration tests spin up PostgreSQL + pgvector via Testcontainers. No external database service required in CI.

**No credentials in source.** All credentials are read from environment variables. `infra/.env.example` documents required variables with placeholder values. Actual `.env` files are excluded by `.gitignore`.

**OpenAPI disabled in production.** SpringDoc active only in non-prod profiles. Both `/swagger-ui.html` and `/v3/api-docs` return HTTP 404 when `SPRING_PROFILES_ACTIVE=prod`.

**TypeScript strict mode.** `"strict": true` in `tsconfig.json`. `@typescript-eslint/no-explicit-any` set to `error`. The build fails if either is not met.

---

## Completion criteria

Phase 0 is complete when all nine issues are closed and all of the following hold:

1. Set env vars + `docker compose up -d` (from `infra/`) + `./mvnw spring-boot:run` produces a running backend with no manual steps beyond providing credentials.
2. `npm install && npm run dev` in the frontend repo produces a running frontend.
3. CI is green on `main` in both repositories.
4. No secrets exist anywhere in either repository.
5. The ArchUnit test passes in the backend.
6. The Flyway migration integration test passes.
7. A pull request with a deliberate test failure cannot be merged to `main` in either repository.

Phase 1 does not begin until all seven criteria are met.