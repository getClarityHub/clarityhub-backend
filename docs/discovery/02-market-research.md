# Market Research: Permission Architecture & Competitive Landscape

**Status:** Complete  
**Date:** 2026-06-25  
**Phase:** Discovery

## Key finding

The brief's claim that competitors handle permissions only after retrieval 
is not supported as a general market claim. Notion and Elastic document 
retrieval-time permission controls. Microsoft uses a deliberate hybrid model.

## What this changes

The original differentiator must be sharpened. The defensible claim is not 
"competitors filter late" but "ClarityHub provides architecturally provable, 
auditable, low-latency access control inside retrieval, with explicit source 
tracing that competitors do not publicly document."

## Competitor summary

| Competitor | Enforcement model | Evidence strength |
|---|---|---|
| Microsoft 365 Copilot | Indexed ACLs + real-time federation | High |
| Notion AI | Query-time permission check | High |
| Elastic | Document-level security in search engine | High |
| Glean | Permission-aware, exact enforcement point unclear | Medium |
| Guru | DLP + RBAC, exact retrieval path unclear | Medium |

## Critical assumptions (revised after research)

1. Enterprise buyers prioritise auditable enforcement over generic 
   "permission-aware" claims. Falsified if: buyers cannot distinguish 
   between retrieval-time and post-retrieval filtering in procurement.

2. Revocation latency matters to enterprise security teams. Falsified if: 
   buyers accept eventual consistency (hours) as sufficient.

3. ClarityHub's open architecture (swappable LLM/embedding providers) is a 
   meaningful differentiator for security-conscious buyers. Falsified if: 
   buyers default to Microsoft/Google regardless of provider flexibility.

## Decision

**Continue with revised positioning.** The market is real, the problem is 
real, and the differentiator is real but needs to be stated more precisely. 
Update the brief and proceed to Requirements.

## Full research report

See `docs/discovery/research/deep-research-full.md` for the complete 
Deep Research output.
