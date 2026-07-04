# Tasks: Visual Unification of Product Card

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~50-80 |
| 400-line budget risk | Low |
| Chained PRs recommended | No |
| Suggested split | Single PR |
| Delivery strategy | ask-always |
| Chain strategy | pending |

Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: pending
400-line budget risk: Low

## Phase 1: DOM Restructure

- [x] 1.1 `producto-cards.html` — Move `<a.btn-comprar>` back inside `<div.card-body>` as last child (reverts nested anchor: outer `<a.producto-card>` wrapping `<a.btn-comprar>` is invalid HTML). Preserve all attributes.

## Phase 2: CSS Refactor

- [x] 2.1 `index.html` L116 — Change `.card-body { padding: 0 }` → `padding: 10px 14px 0`
- [x] 2.2 `index.html` L117-119 — Remove 3 rules: `.card-body > *`, `> :first-child`, `> .btn-comprar` margin hacks
- [x] 2.3 `index.html` L156 — Remove `border-radius: 10px` from `.btn-comprar`
- [x] 2.4 `index.html` — Convert `.producto-card` to `display: flex; flex-direction: column`
- [x] 2.5 `index.html` — Add `flex: 1` to `.card-body` to push button to bottom
- [x] 2.6 `index.html` — Redesign `.btn-comprar`: fixed 38px height, `margin-top: auto`, `flex-shrink: 0`, line-height centering, removed `min-height` and `display: block`

## Phase 3: Manual Verification

- [ ] 3.1 Visual check — Uniform 14px border-radius, button corners clipped by parent `overflow: hidden`
- [ ] 3.2 Functional — WhatsApp 💬 click triggers `stopPropagation()`, card parent navigation prevented
- [ ] 3.3 Regression — Card click still navigates to detalle, long names stay contained at 360px
- [x] 3.4 Build — `mvnw.cmd clean compile` passes
