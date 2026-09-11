# Can subscribers of the new NOWs API trust it to explain what's on the document?

*Summary. For the full technical evidence, see the companion document,
[2026-09-11-now-document-generation-correlation-analysis-technical.md](2026-09-11-now-document-generation-correlation-analysis-technical.md).*

## Context

A NOW is a formal notice, order, or warrant sent out after a court result — for example, a warrant
telling a prison to hold someone in custody. There are 40 NOW document types. We're proposing an API
that lets subscribers (prisons, other justice-system organisations) read the case data behind a NOW
directly, rather than receiving the document via HRDS.

## Question

If a subscriber reads that case data through the API, can they reliably determine what the actual
document says — the same facts, combined the same way, in the same order, as the real document?

**Answer: no, not today.** This is structural, not a defect in one document type.

## How this was assessed

One real example (a remand warrant, following a bail appeal) was traced end to end: the case data, the
template it renders through, the document produced, and the message sent to the downstream system that
notifies subscribers. The pattern found there was then checked against all 40 document types.

## Findings

**1. Sentence construction happens outside the case data.** A document-generation system, separate from
Common Platform, decides which facts appear in which sentence and in what order, reading a template.
None of that ordering or grouping exists in the case data itself — only the document-generation system
knows it. One worked example: a single paragraph on the real warrant combines a caption, the warrant
date, a conditional "Amended on" clause that only appears if the warrant was later amended, a reused
instance of the same warrant date for an unrelated purpose, and two blocks of fixed legal wording —
stitched together by the template, with nothing in the case data indicating any of this structure.

**2. For roughly a third of the 40 document types, only one pre-selected result is ever shown — the
rest are silently ignored.** Which result is shown is fixed in the template's design, matched against a
value the case data does not expose. A case can have several relevant results recorded; the API cannot
tell a subscriber which one (if any) the document actually printed.

**3. Almost none of a NOW's actual content currently reaches HRDS**, the downstream system that
notifies consumers such as Prisons that a document exists. Traced for the same example: only the
person's name, date of birth, and case reference number are passed on today — none of the order or
warrant's substance.

**4. Part of the document's wording is not case data at all.** Standing legal phrasing (e.g. the
48-hour appeal rule) is fixed per document type and lives only in the template. This is expected and
not a defect — but it means even a subscriber unaffected by points 1–3 would only ever receive
case-specific facts, never a full reproduction of the document text.

## Business impact

A consumer who treats the API's data as equivalent to the document will sometimes act on an incomplete
or wrongly-assembled picture of what the document says, with no signal in the data indicating when
that's happened.

## Decision required

- **Scope the API to what it delivers today**: case data only, explicitly not a substitute for the
  document, with the two able to diverge in ways subscribers cannot detect.
- **Close the gap before extending the claim**: build the missing link between case data and rendered
  document text before describing the API as showing "what the document says."

This document does not choose between the two — that decision sits with the business.
