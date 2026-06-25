# 03 — Discovery Decision

**Status:** Complete  
**Date:** 2026-06-25  
**Phase:** Discovery — closing document  
**Author:** christofferka  
**Closes:** Discovery phase. Next phase: Requirements.

---

## Decision

**Continue with revised positioning. Proceed to Requirements.**

The problem is real. The market is real. The technical approach is sound. One element of the original positioning requires correction before Requirements begins. Everything else stands.

---

## What research confirmed

The core problem ClarityHub solves is validated. Enterprise organisations accumulate knowledge across scattered documents, employees waste time finding it, and generic AI tools make the situation worse by answering from training data rather than actual company documents and with no concept of who is allowed to see what.

The market signal is strong. Glean's valuation moved from $2.2 billion to $4.6 billion in seven months in 2024. Gartner findings show that security and oversharing concerns materially delayed Microsoft 365 Copilot deployments in many organisations. Enterprise buyers are willing to pay, but only when governance and data control are credible.

The technical architecture is correct. Enforcing access control inside retrieval rather than after it is the right approach, confirmed by both competitor behaviour and recent academic research. The OrgAccess (2025) benchmark demonstrates that LLMs perform poorly on complex organisational permission reasoning, which means permission logic must live in deterministic systems, not in prompt instructions. The HoneyBee (2025) paper shows that how permission-aware retrieval is implemented is itself an architectural differentiator. Clean Architecture with swappable LLM and embedding ports is validated as a meaningful enterprise selling point.

The pgvector decision is confirmed. Keeping embeddings in PostgreSQL alongside access grants makes permission-scoped retrieval a single SQL query. There is no window where unauthorised content is fetched and discarded. That is the architectural fact the product's promise rests on.

---

## What research corrected

The original positioning stated that competitors "retrieve broadly and only narrow access after retrieval." This is not supported as a general market claim and must not be used in positioning or marketing.

Notion explicitly documents query-time permission validation. Elastic embeds document-level and field-level security inside the search engine itself. Microsoft uses a deliberate hybrid of indexed ACLs and real-time federated retrieval. The claim that all major competitors rely on post-retrieval filtering is false, and a competitor or enterprise security team would disprove it immediately.

This does not weaken ClarityHub's position. It sharpens it.

---

## Revised positioning

The claim that goes into Requirements, architecture documents, and any future marketing material is:

> ClarityHub provides provable, auditable, low-latency access control enforced inside retrieval itself, with explicit source tracing and compliance-grade governance that competitors do not publicly document to a verifiable standard.

The five defensible and buildable differentiators are:

**Provable retrieval-time enforcement.** Access control happens in the SQL query that performs vector similarity search. It is not a post-retrieval step. This can be demonstrated with adversarial tests that prove a lower-privileged user cannot extract higher-privileged content under any retrieval path.

**Low revocation latency.** Notion publicly discloses up to one hour for permission changes to propagate. ClarityHub targets near-real-time revocation for critical systems, with observed latency exposed in the admin dashboard and audit log. This is a measurable and believable claim.

**Explicit source tracing.** Every answer shows which chunks were retrieved, from which documents, with which similarity scores, and under which access rules. Enforcement is visible and auditable, not a black box.

**Defence in depth.** Permissions alone do not eliminate RAG security risks. ConfusedPilot (2024), EchoLeak (2025), and SearchLeak (June 2026) document real-world exfiltration attacks against enterprise RAG systems with correct user permissions in place. ClarityHub's requirements must include output sanitisation, provenance tracking, prompt injection protection, and anomaly detection as first-class requirements, not afterthoughts.

**Provider independence.** Swappable LLM and embedding ports allow organisations to choose a deployment where no document content leaves their boundary. This is a direct response to the security concern that delayed Copilot adoption in many enterprises.

---

## What this means for Requirements

Three things follow directly from this decision for the Requirements phase.

First, the adversarial test suite is non-negotiable from Phase 1. Every feature that touches retrieval, search, or chat must have tests that actively attempt to extract content a lower-privileged user should not be able to see. Any leakage is a release blocker.

Second, fast permission revocation must be treated as a Phase 2 functional requirement, not a nice-to-have. The access grant system must propagate changes quickly, and the latency must be measurable and visible.

Third, defence-in-depth controls (output sanitisation, provenance tracking, prompt injection protection) are added to the Phase 2 and Phase 3 scope. Phase 2 covers identity and access in full. These controls are an addition to that scope, not a replacement.

---

## What remains unchanged

The phased build plan is unchanged. Phase 1 vertical slice first, with permission enforcement present from the first query.

The technology decisions are unchanged. Java 21 / Spring Boot, React / TypeScript, PostgreSQL with pgvector, Docker, GitHub Actions, Azure, Clean Architecture with port and adapter pattern.

The three personas are unchanged. Dana, Marcus, and Priya remain the governing design reference for every feature decision.

The non-functional requirements are unchanged and remain the standard every phase is held to: TLS everywhere, secrets in a vault, Argon2id password hashing, rate limiting on auth and AI endpoints, stateless backend, structured logging, request tracing, and meaningful test coverage at unit, integration, and end-to-end levels.

---

## Discovery phase closed

| Document | Status |
|---|---|
| `01-problem-framing.md` | Complete |
| `02-market-research.md` | Complete |
| `research/deep-research-full.md` | Complete |
| `03-discovery-decision.md` | Complete |

**Next action:** Begin Requirements phase. First deliverable: Phase 1 specification broken into user stories, acceptance criteria, architecture considerations, security considerations, and test strategy for each of the eight vertical slice tasks.
