# Proposal: Unificación visual del producto-card

## Intent

Product card renders as 3 visual blocks (image, details, button) with inconsistent radii — button's `10px` vs container's `14px`. Fix unifies the card into a single visual block, improving UX consistency and polish.

## Scope

### In Scope
- Move `.btn-comprar` outside `.card-body` to direct child of `.producto-card`
- Eliminate button border-radius, replace margin hacks with padding on `.card-body`
- Verify `event.stopPropagation()` and long product names

### Out of Scope
- `detalle.html` (.rel-card), colors, hover, shadows, responsive — preserved as-is
- Moving CSS to `main.css` — deferred

## Capabilities

> Pure CSS/HTML visual refactor — no spec-level requirements change.

### New Capabilities
None

### Modified Capabilities
None

## Approach

**Approach 2 from exploration (Restructure HTML + CSS):**
1. Move `<a.btn-comprar>` out of `.card-body`, sibling to it under `.producto-card`
2. Remove `border-radius` from `.btn-comprar` — `overflow: hidden` on parent handles corners
3. `.card-body`: `padding: 10px 14px 0` (was `0` with margin hacks on children)
4. Remove `card-body > * { margin: 0 14px }`, `card-body > :first-child`, `card-body > .btn-comprar` hacks
5. Add `.btn-comprar { margin-top: 10px }` for spacing

Result: one container, one border-radius (14px), no internal borders, bottom corners match.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `fragments/producto-cards.html` | Modified | Move button outside card-body |
| `index.html` (CSS L86-164) | Modified | Remove button border-radius, simplify card-body |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| `stopPropagation()` breaks after DOM move | Low | Same structure (a inside a), selector unchanged |
| Long names overflow after padding change | Low | Verify with sample long name |
| Responsive columns break | Low | Only padding/margin changes, not widths |

## Rollback Plan

`git checkout` on the two files — no data or backend logic involved.

## Dependencies

None.

## Success Criteria

- [ ] Single visual block with uniform 14px border-radius, button blends into bottom
- [ ] No margin hacks in card-body — spacing via `padding` only
- [ ] WhatsApp click works without triggering card navigation
- [ ] Long names don't overflow; 2-column mobile layout preserved
