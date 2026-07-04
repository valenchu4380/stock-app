# Design: Fix Cart Overlay Scroll

## Technical Approach

Four independent CSS/JS fixes to the product listing page. No backend changes. All changes are in `index.html` (DOM reorder + JS scroll lock) and `main.css` (variable fix + touch-action + no-scroll class).

---

## Architecture Decisions

### Decision: Scroll Lock Mechanism

| Option | Tradeoff | Decision |
|--------|----------|----------|
| CSS class toggle + iOS workaround | Simple, no JS timer, compatible everywhere | ✅ **Chosen** |
| Inline style via JS | Clutters DOM, harder to debug | ❌ Rejected |
| `overflow: hidden` on `<html>` | Fails on iOS Safari body scroll | ❌ Rejected |

**Rationale**: `.no-scroll` class with `overflow: hidden` works on all modern browsers. iOS Safari ignores `overflow: hidden` on `<body>` — the `position: fixed; top: -scrollY; width: 100%` pattern is the proven workaround. Save/restore `scrollY` in `abrirCarrito`/`cerrarCarrito`.

### Decision: Touch-action Scope

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Group selector in main.css | One rule, zero HTML changes | ✅ **Chosen** |
| Utility class per element | Increases HTML churn, easy to miss elements | ❌ Rejected |

**Rationale**: `button, a, input, select, .touch-target { touch-action: manipulation }` covers ALL interactive elements site-wide in one declaration. Zero HTML changes. `manipulation` removes 300ms tap delay and double-tap zoom without affecting scroll or text selection.

### Decision: CSS Variable Strategy

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Direct `var(--color-fondo)` | Matches existing palette, works immediately | ✅ **Chosen** |
| Define `--bg-primary` in `:root` | Adds unused var, more diff | ❌ Rejected |

**Rationale**: `--color-fondo` is already defined in `:root` (main.css line 9). The sticky bar bg should match the page bg — using the same variable is correct. This is a one-line change in `.sticky-bar`.

### Decision: DOM Reorder

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Move `<div class="sticky-bar">` after `.hero` | Simple, semantic, CSS works at new position | ✅ **Chosen** |
| CSS-only (order property, position tricks) | Can't change tab order; a11y broken | ❌ Rejected |

**Rationale**: CSS `order` only works on flex/grid children and doesn't change DOM tab order. The search `<input>` is inside the sticky bar — keyboard focus and screen reader order MUST follow visual order. DOM move is the only correct approach.

---

## Data Flow

```
DOMContentLoaded
  └→ abrirCarrito()
       ├→ overlay.removeAttribute('hidden')
       ├→ document.body.classList.add('no-scroll')       ← NEW
       ├→ scrollY = window.scrollY                       ← NEW (iOS)
       └→ trapFocus(overlay)
       └→ cerrarCarrito()
            ├→ overlay.setAttribute('hidden', '')
            ├→ document.body.classList.remove('no-scroll') ← NEW
            ├→ window.scrollTo(0, savedScrollY)           ← NEW (iOS)
            └→ focus restoration

Scroll lock:  DOM event → JS classList toggle → CSS :: no re-render needed
Touch-action: CSS rule (static) → browser applies to all matching elements
```

---

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `src/main/resources/templates/index.html` | Modify | Move `.sticky-bar` block (lines 514–534) after `.hero` block (line 542); add scroll-lock logic in `abrirCarrito`/`cerrarCarrito` |
| `src/main/resources/static/css/main.css` | Modify | Change `.sticky-bar` background to `var(--color-fondo)`; add `.no-scroll` class; add `touch-action: manipulation` group selector |

---

## Testing Strategy

Manual verification only — no JS test infrastructure exists.

| Check | How |
|-------|-----|
| Sticky bar position | Visual: bar appears below hero, sticks on scroll |
| Sticky bar background | Visual: no white seam, matches page bg |
| Body scroll locked | Open overlay → try to scroll (wheel + touch) |
| iOS Safari scroll | Test on real device: open overlay at different scroll positions |
| Scroll position restored | Close overlay → page is at same scroll Y |
| No double-tap zoom | Tap buttons/inputs/links rapidly on mobile — no zoom |
| Touch-action on selects | Category picker scrolls through options normally |
| Tab order | Tab through page: hero → search → filters → products → cart icon |
| Existing filters/search | Type in search, change category — still submits correctly |
| Infinite scroll | Observer still attached to sentinel, works with sticky bar above |

---

## Open Questions

- [ ] Confirm iPhone Safari version for testing (iOS 15.4+ has correct `position: fixed` behavior)
- [ ] Does the infinite scroll `IntersectionObserver` fire reliably when the sticky bar is above the grid? (The sentinel is at the grid bottom — should be fine, but verify)
