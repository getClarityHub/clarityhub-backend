# ClarityHub — Problem Framing Document

**Phase:** Discovery
**Status:** Written before research. Preserved as a point-in-time record.
**Document type:** Problem framing (not a requirements spec, roadmap, or competitive analysis)

---

## Why this document exists

This is the first artifact in the ClarityHub discovery process. It captures the
problem framing **before any structured research has been conducted**, so that when
research findings arrive there is an honest before-and-after record of what was
assumed versus what was found.

It is deliberately preserved unchanged after the research runs. A framing document
edited to match the findings would defeat its own purpose. The value of this file is
that it is timestamped to a moment of uncertainty.

> **A note on intellectual honesty.** This document records a reasoning error I made
> early in the project and the correction that followed. That is intentional. The
> ability to catch your own inherited assumptions before they propagate into
> architecture is the skill this project is meant to demonstrate. A clean story with
> no visible course-correction would be less truthful and less useful.

---

## The dual purpose of this project (stated openly)

ClarityHub is two things at once, and pretending otherwise would undermine the
credibility of everything downstream:

1. **A genuine product discovery exercise.** The research is real, the questions are
   real, and the findings are allowed to change the product direction.
2. **A portfolio project** built to demonstrate structured, professional 
   systems design and product management process to a technical reviewer 
   or hiring manager.

These purposes are not in conflict. A rigorous discovery process is exactly what a
senior practitioner would run regardless of whether the product ships. But honesty
requires that both purposes are visible rather than hidden.

---

## The problem, in two layers

There is a surface problem and a deeper problem. They are related but not the same,
and conflating them leads to building the wrong thing.

### Layer 1 — The surface problem (real, but already crowded)

Companies accumulate knowledge in PDFs, policies, contracts, and meeting notes.
Employees waste hours searching for answers, interrupt colleagues to ask, or make
decisions without the relevant information in front of them.

This is a real and widely felt problem. It is also already addressed by many existing
products. Solving it alone is not a sufficient basis for a new product, and a reviewer
would be right to be unimpressed by a tool that only does this.

### Layer 2 — The deeper problem (the actual target)

Generic AI tools make the surface problem **worse** in a specific and dangerous way.
They answer confidently from training data rather than from the company's actual
documents, and they have no concept of who is permitted to see what.

This creates a specific enterprise risk: a response that quietly contains content from
a document the employee was never authorized to read. This is commonly called
**permission leakage**. The claim motivating this project is that leakage is the
problem that causes security teams to block AI adoption in enterprise environments,
and that no existing solution handles it rigorously.

That claim is examined critically in the next section.

---

## The core hypothesis (and where it came from)

ClarityHub's premise rests on a single testable claim:

> Existing enterprise AI knowledge tools do not enforce document-level permissions
> rigorously enough to satisfy the security requirements of compliance-sensitive
> organizations, and this gap is both real and felt by the buyers who would evaluate a
> product like ClarityHub.

**This hypothesis was inherited, not derived.** It emerged from early research into
the enterprise AI knowledge management space, where vendor marketing and industry
commentary frequently assert that leakage is what enterprises fear and that existing
tools have no concept of who is allowed to see what. Early in this project those
assertions were treated as established fact and built upon: the competitor framing, the
positioning language, and the assumption mapping all rested on claims that had not been
independently verified.

That was a real error, and it matters practically, because an unexamined premise
propagates silently into architecture decisions. The correction is to treat the premise
as a hypothesis to be tested, and to design the research so that it is **capable of
disproving the premise,** not just confirming it.

---

## Honest priors, before research

These are my genuine intuitions before any structured research. They are stated
plainly to create a baseline the findings can be measured against.

| # | Prior | Confidence |
|---|-------|-----------|
| 1 | Existing enterprise vendors (Microsoft, Google, Elastic and similar) are probably reasonably good at permission enforcement. They have large security teams and strong compliance incentives. The assumption that they handle it poorly should be treated as unverified. | Medium |
| 2 | Enforcing permissions *inside* the retrieval operation rather than as a post-filter is technically sound and architecturally cleaner. Whether it is meaningfully better in practice, and whether buyers care, is unknown. | Medium |
| 3 | If a real gap exists, it is likely **narrow** rather than broad. A narrow, real, defensible gap is more valuable than a broad invented one, and more honest. | Medium |
| 4 | The build-versus-buy question may be the most important one. If pgvector, open-source embedding models, and RAG frameworks are freely available, a sophisticated enterprise might simply build this themselves. Whether ClarityHub is a *product* or a *feature* is genuinely open. | Low to Medium |

> **The uncomfortable summary:** my honest prior is closer to "existing solutions are
> probably good enough" than to "nobody has solved this." I am building a product whose
> central claim my own intuition is skeptical of, and commissioning research to find out
> which is closer to the truth. That is the most credible possible footing for a
> discovery process: not an advocate for the idea, but an investigator of it.

---

## What the research must determine

These questions must be answered before any architecture decision is locked or any
positioning language is finalized.

1. **Is permission leakage theoretical or demonstrated?** Are there documented cases of
   enterprise AI tools surfacing unauthorized content, and if so, what caused them?
2. **Where do competitors enforce permissions?** Before retrieval, after retrieval, or
   inside the retrieval operation? This determines whether ClarityHub is genuinely
   differentiated or functionally equivalent to what exists.
3. **Do buyers care about the distinction?** A technical advantage that buyers cannot
   evaluate, or do not weight in purchasing decisions, is not a commercial advantage.
4. **What regulatory requirements apply**, and do they create a compliance argument for
   ClarityHub's approach that would resonate in enterprise procurement?
5. **Would a target enterprise build this themselves?** If yes, under what conditions
   and at what cost? If no, why not?

---

## What would change the product direction

Since ClarityHub will be built regardless as a portfolio project, the findings do not
determine **whether** to build. They determine **what** to build and how to position it
honestly.

- **If the gap is real, felt, and unaddressed:** proceed as designed, with
  permission-scoped retrieval as the primary differentiator.
- **If existing solutions are adequate for most use cases:** reposition. ClarityHub
  becomes a demonstration of clean architecture, with permission enforcement as a case
  study in doing something *correctly* rather than a claim of doing something nobody
  else does.
- **If the gap exists but is narrow:** focus on the specific conditions where
  in-retrieval enforcement matters most. Likely candidates: high-compliance
  industries, rapid permission-change environments, or organizations with complex
  nested access structures.

Any of these is a credible, honest outcome. The only failure mode is ignoring findings
that complicate the premise and presenting a clean story a reviewer could disprove in
minutes.

---

## What this document is not

This is not a requirements specification, a product roadmap, or a competitive
analysis. Those come *after* the research, informed by what it finds. This document
exists solely to record the thinking that preceded the research, so the research can be
evaluated as genuine discovery rather than retrospective justification.
