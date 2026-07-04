# Design: Visual Unification of Product Card

## Technical Approach

Restructure `producto-cards.html` to move `<a.btn-comprar>` from child of `.card-body` to sibling (direct child of `.producto-card`). Replace margin-based hacks with padding on `.card-body`. Remove `border-radius: 10px` from `.btn-comprar` — `.producto-card`'s `overflow: hidden` clips button corners. Spacing between `.card-body` and `.btn-comprar` via `margin-top: 10px` on the button.

## Architecture Decisions

### Decision: Button DOM position

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Move outside `.card-body` as sibling | Clean structure. Button spans full width via parent. No margin hacks needed. | ✅ **Selected** |
| Keep inside `.card-body` with CSS-only fix | Requires negative margins or `width: calc(100% + 28px)` for edge-to-edge. Margin overrides remain. | ❌ Rejected |

### Decision: Button border-radius

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Remove `border-radius` altogether | Parent `overflow: hidden` clips bottom corners to 14px. Focus outline clipped — still functional. | ✅ **Selected** |
| Keep `border-radius: 10px` | Two visual blocks. Inconsistent radii (10px vs 14px). Defeats purpose. | ❌ Rejected |

### Decision: Card-body spacing model

| Option | Tradeoff | Decision |
|--------|----------|----------|
| `padding: 10px 14px 0` on `.card-body` | Single indentation source. No child selectors needed. Simplifies CSS. | ✅ **Selected** |
| Current `margin`-based system | Requires `> *` universal selector, `:first-child` hack, and per-element overrides. Fragile. | ❌ Rejected |

### Decision: Button spacing from card-body

| Option | Tradeoff | Decision |
|--------|----------|----------|
| `margin-top: 10px` on `.btn-comprar` | Simple, decoupled, no impact on card-body padding. | ✅ **Selected** |
| `padding-bottom` on `.card-body` | Wrong layer — indents inside card-body, not between siblings. | ❌ Rejected |

## Data Flow

No data flow changes. Thymeleaf model binding, event handlers, and href attributes remain identical.

```
Controller → model → Thymeleaf → producto-cards.html
                                     ↓
            .producto-card (same attributes, same children, reordered)
              ├── .card-img ────────────── (unchanged)
              ├── .card-body ───────────── (padding: 10px 14px 0, no margin hacks)
              └── .btn-comprar ─────────── (moved outside, no border-radius, margin-top: 10px)
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `src/main/resources/templates/fragments/producto-cards.html` | Modify | Move `<a.btn-comprar>` after `</div>` closing `.card-body` |
| `src/main/resources/templates/index.html` (embedded CSS, L86-164) | Modify | Remove border-radius from button, simplify card-body rules |

0 new, 2 modified, 0 deleted.

## CSS Diff (index.html `<style>` section)

| Current (line) | Change |
|----------------|--------|
| L116: `padding: 0` | → `padding: 10px 14px 0` |
| L117: `> * { margin: 0 14px }` | **Remove** entire rule |
| L118: `> :first-child { margin-top: 10px }` | **Remove** entire rule |
| L119: `> .btn-comprar { margin-left: 0; margin-right: 0 }` | **Remove** entire rule |
| L156: `border-radius: 10px` (in `.btn-comprar`) | **Remove** line |
| *(after L164)* | **Add** `.btn-comprar { margin-top: 10px; }` |
| *(after L164)* | **Add** `.btn-comprar.focus-visible:focus-visible { border-radius: 0; }` |

## HTML Diff (producto-cards.html)

**Before:**
```html
<div class="card-body">
  <div class="card-title">...</div>
  <span class="card-brand">...</span>
  <div class="card-price">...</div>
  <div class="card-stock-status">...</div>
  <a class="btn-comprar touch-target focus-visible"
     th:href="..." target="_blank"
     onclick="event.stopPropagation()">💬 Comprar</a>
</div>
```

**After:**
```html
<div class="card-body">
  <div class="card-title">...</div>
  <span class="card-brand">...</span>
  <div class="card-price">...</div>
  <div class="card-stock-status">...</div>
</div>
<a class="btn-comprar touch-target focus-visible"
   th:href="..." target="_blank"
   onclick="event.stopPropagation()">💬 Comprar</a>
```

## Key Behavioral Guarantees

| Concern | Why It Works |
|---------|-------------|
| `stopPropagation()` | `onclick` is on the element itself. Event bubbles `.btn-comprar` → `.producto-card` regardless of sibling relationship. |
| Long names | `.card-title { word-break: break-word }` or `overflow-wrap` — no change, padding provides same 14px inset. |
| Stock opacity on `.sin-stock` | Class stays on `.producto-card`, opacity cascades to all children. |
| Touch target (44px) | `.touch-target` class and `min-height: var(--touch-min)` on `.btn-comprar` unchanged. |
| Focus ring | `.focus-visible:focus-visible` applies `border-radius: var(--radius)`. Overridden with `border-radius: 0` for `.btn-comprar` to keep visual purity. |

## Interfaces / Contracts

No interfaces, types, or data contracts change. All Thymeleaf attributes (`th:each`, `th:href`, `th:text`, `th:classappend`, `th:aria-label`) are preserved.

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Visual | Uniform 14px border-radius, no button radius | Manual visual check in Chromium/Firefox at 360px and desktop |
| Functional | WhatsApp click doesn't navigate card | Click 💬 icon → verify `stopPropagation()` prevents `<a.producto-card>` navigation |
| Functional | Card click still navigates to detail | Click card image or body → verify `detalle` navigation |
| Regression | Long product names stay contained | Render product with 60+ char name at 360px viewport |
| Regression | Touch target still ≥44×44px | Inspect computed `min-height` on `.btn-comprar` |

No automated frontend tests exist in this project (no JS framework — pure Thymeleaf). Visual regression would require a manual diff.

## Migration / Rollout

No migration required. Rollback: `git checkout` on the two files. No data, no backend, no DB.

## Open Questions

- **Focus ring aesthetics**: Removing `border-radius` from `.btn-comprar` means the focus-visible outline ring has no radius of its own (overridden to 0). The parent's `overflow: hidden` clips the bottom of the ring. This is functionally acceptable but worth a visual check. If the square-corner ring looks jarring, we can add `outline-radius` equivalent via `box-shadow` simulation — **deferred until visual review**.

- **`.card-body` margin-bottom edge case**: Removing `.card-body > *` universal margin means elements with their own `margin-bottom` (e.g., `.card-price` has `margin: 4px 0`) could protrude past the `padding-bottom: 0` of `.card-body`. In practice `.card-price`'s `4px` bottom margin will collapse or be absorbed — verified during implementation.
