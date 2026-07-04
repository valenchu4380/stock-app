# Proposal: Fix Cart Overlay Scroll

## Intent

Fix 4 UX bugs on the product listing page: body scrolls behind cart overlay, mobile double-tap zoom on cart elements, harsh white sticky bar background from undefined CSS variable, and search bar appearing above the hero banner instead of below it.

## Scope

### In Scope
- Body scroll lock when cart overlay is open (with iOS Safari workaround)
- Site-wide `touch-action: manipulation` on all interactive elements
- Sticky bar background changed to use `--color-fondo` instead of undefined `--bg-primary`
- Move `.sticky-bar` DOM element below `.hero` in index.html

### Out of Scope
- Scroll-lock for other modals/overlays (admin panels, detail views)
- Changing viewport meta tag (`user-scalable=no`)
- Infinite scroll behavior changes
- Touch-action refinements for non-interactive elements

## Capabilities

### New Capabilities
- `scroll-lock`: Body scroll lock for overlays and modals, with iOS Safari `position:fixed` workaround and `scrollY` save/restore
- `touch-behavior`: Site-wide `touch-action: manipulation` on all interactive elements to eliminate 300ms tap delay and double-tap zoom

### Modified Capabilities
- `product-listing`: Sticky search bar position changes (starts below hero, sticks from there); cart overlay gains scroll-lock requirement

## Approach

1. **Scroll lock** — Add `.no-scroll` CSS class (`overflow: hidden; position: fixed; width: 100%; top: -scrollY`) toggled in `abrirCarrito()`/`cerrarCarrito()`. Save/restore `scrollY` for iOS.
2. **Touch zoom** — Add `touch-action: manipulation` to `.touch-target`, `button`, `a`, `input`, `select` in main.css site-wide.
3. **Sticky bg** — Change `background: var(--bg-primary, #fff)` to `background: var(--color-fondo)` in `.sticky-bar`.
4. **Search position** — Move `.sticky-bar` DOM block (lines 514–534) after `.hero` closing tag (line 541) in index.html.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `index.html` | Modified | Move `.sticky-bar` after `.hero`; add scroll lock JS in `abrirCarrito`/`cerrarCarrito` |
| `main.css` | Modified | `.sticky-bar` background fix; add `.no-scroll` class; add `touch-action: manipulation` to interactive elements |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| iOS Safari ignores `overflow:hidden` on body | Medium | Use `position:fixed` + `top:-scrollY` pattern, test on real iOS |
| Search bar DOM move breaks JS assumptions | Low | Grep for `.sticky-bar`, `#filtroForm`, or `document.forms[0]` references |
| Touch-action on selects/inputs breaks native behavior | Low | `manipulation` only disables double-tap zoom, not scrolling or selection |

## Rollback Plan

Revert commit. Each fix is independent — partial rollback is safe: JS scroll lock, CSS background value, touch-action declarations, and HTML DOM reorder are all isolated changes.

## Dependencies

None.

## Success Criteria

- [ ] `.sticky-bar` appears below the hero banner and sticks from there on scroll
- [ ] Body does NOT scroll when cart overlay is open (desktop + iOS Safari)
- [ ] No double-tap zoom on any interactive element (button, a, input, select) on mobile
- [ ] `.sticky-bar` background matches page background `#F9F3EC` (no white seam)
- [ ] All existing tests pass
