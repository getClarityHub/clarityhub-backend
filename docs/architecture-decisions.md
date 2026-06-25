# Architecture Decisions

**Status:** Active  
**Date:** 2026-06-25  
**Author:** christofferka

Significant technical decisions for ClarityHub. Changes are appended, never overwritten.

---

## ADR-001 — pgvector over a dedicated vector database

**Decision:** Embeddings live in PostgreSQL via pgvector, not a separate vector store.

**Reason:** Permission-scoped retrieval requires that the access filter and the vector search run in the same query against the same source of truth. A separate vector database makes this impossible without duplicating the permission model into a second system or falling back to post-retrieval filtering, which is exactly what the product exists to avoid. One datastore also means one thing to operate, back up, and secure.

**Alternatives considered:** Pinecone, Weaviate, Qdrant. All ruled out for the reason above. Revisit only if pgvector cannot meet the p95 latency target after indexing and tuning.

---

## ADR-002 — Clean Architecture with port and adapter pattern

**Decision:** Four layers: Domain, Application, Infrastructure, Presentation. Dependencies point inward only. All external dependencies are accessed through ports defined in Application and implemented as adapters in Infrastructure.

**Reason:** Swapping the LLM or embedding provider must require writing one new adapter and changing zero domain or application code. That is the test of whether the boundaries are real. It is also the credibility requirement for enterprise buyers who need the option of a deployment where no document content leaves their boundary.

**Alternatives considered:** Conventional layered architecture without enforced port boundaries. Ruled out because it makes provider swapping expensive later and tightly couples business logic to early infrastructure choices.

---

## ADR-003 — Single datastore

**Decision:** PostgreSQL handles everything: relational data, full-text search, vector embeddings, audit log. No additional datastores until a measured problem justifies one.

**Reason:** Every additional datastore is a new synchronisation boundary. The most dangerous failure mode for ClarityHub is a permission model that diverges between systems. One datastore eliminates that class of failure entirely.

**Revisit if:** a specific, benchmarked bottleneck cannot be resolved within PostgreSQL. Document the problem before introducing anything new.

---

## ADR-004 — Phased build, vertical slice first

**Decision:** Phase 1 is a single end-to-end slice: login, upload one PDF, chunk and embed it, ask a question, get a cited answer, with permission enforcement in the retrieval query from the first commit. All subsequent phases extend that working spine.

**Reason:** Building everything to production quality simultaneously is the primary risk for a solo engineer on this stack. The vertical slice proves the architecture end to end while the system is small enough to reason about completely. Everything after it is extension, not new risk.

**Rule:** scope does not broaden until the current phase is solid.

---

## ADR-005 — Permission enforcement inside retrieval, never after

**Decision:** Access control is enforced inside the vector similarity search query, not as a post-retrieval step. Unauthorised chunks are never fetched. They are never in the prompt. The LLM physically cannot reference them.

**Reason:** Any design that fetches first and filters second has already exposed restricted content to the retrieval layer. That breaks the product's core guarantee regardless of what happens next.

**Leakage vectors that must be closed and tested:**
- Existence leakage: no response may reveal that a restricted document exists.
- Citation leakage: citations are generated only from the filtered chunk set.
- Search leakage: keyword search uses the identical permission filter as vector search.
- Revocation leakage: access changes propagate before the next query can be served.

**Enforcement:** adversarial tests that actively attempt cross-permission extraction are required from Phase 1. Any leakage is a release blocker, not a bug.

---

## ADR-006 — Stateless backend with JWT authentication

**Decision:** Stateless API, signed JWTs, short-lived access tokens with refresh rotation. No server-side session state.

**Reason:** Horizontal scaling without session affinity. The signing secret lives in Key Vault, never in code or images. Invalid credentials return a generic error regardless of whether the email exists.

---

## ADR-007 — Azure as the hosting platform

**Decision:** Azure Container Apps for API and workers, Azure Database for PostgreSQL with pgvector, Azure Blob Storage for files, Azure Key Vault for secrets.

**Reason:** Managed PostgreSQL with pgvector support, managed secrets, and container hosting in one platform with the compliance certifications enterprise buyers expect.

**Note:** the application code has no Azure dependency. Infrastructure adapters can be rewritten for AWS or GCP without touching Domain or Application.

---

## ADR-008 — Three governing personas

**Decision:** Dana (Administrator), Marcus (Manager), Priya (Employee) are the governing design reference for every feature decision.

**Reason:** ClarityHub's security guarantee only matters if it maps to real failure modes real people face. Dana's failure mode is an incident report. Marcus's is a document his team restricted showing up in someone else's answer. Priya's is a wrong or unsourced answer she acts on. Keeping these concrete prevents security and product decisions from becoming abstract.

---

## ADR-009 — Grounding transparency over confidence scores

**Decision:** No confidence percentage is displayed. The system shows which chunks were retrieved, the similarity score for each, the source document, and whether the answer is fully grounded in retrieved text or contains model additions beyond the sources.

**Reason:** Calibrated LLM confidence scores are an unsolved problem. A number that looks authoritative but does not track real reliability manufactures false trust. Grounding transparency shows only what is genuinely computable.

---

## ADR-010 — Append-only audit log

**Decision:** Every security-relevant action is written to an append-only log: logins, uploads, deletions, permission changes, and every AI request with the chunks retrieved and the user who triggered it.

**Reason:** The audit log is a security control, not a debugging tool. It is how Dana proves to her security team that no leakage occurred. No application code path may modify or delete entries. Retention is an infrastructure policy, not an application concern.

---

## ADR-011 — Anthropic Claude as the default LLM provider

**Decision:** The default `LlmProvider` adapter targets Anthropic Claude via the Anthropic API. This is the default implementation of a port, not a hardcoded dependency.

**Reason:** Strong instruction-following, reliable long-context handling for multi-chunk RAG prompts, and the ability to be constrained to answer only from provided source text.

**Key rule:** the Anthropic API key is injected at runtime from Azure Key Vault. It is never in code, config files, or container images. If it is ever committed to version control, rotate it immediately.

**Alternatives:** OpenAI and Google Gemini are viable alternatives requiring only a new adapter. A self-hosted open model is the path for customers with a hard requirement that no content leaves their boundary. The port design must accommodate that from Phase 1 even if the adapter does not exist yet.
