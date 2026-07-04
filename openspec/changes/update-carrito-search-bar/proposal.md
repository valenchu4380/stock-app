# Proposal: Update Carrito Search Bar

## Intent

Search bar scrolls away; cart needs full-page navigation. Both hurt browsing. Goal: sticky bar with search+cart, plus an always-accessible cart overlay.

## Scope

### In Scope
- Sticky container bar (search + cart icon) on product listing
- Full cart overlay (bottom-sheet mobile, centered modal desktop) reusing `#dialog-overlay`
- Overlay: items, quantities, total, remove/vacate, WhatsApp buy
- Product card "Comprar" → "Agregar al carrito" (writes to localStorage)
- Cart icon opens overlay on ALL sizes (no navigation to cart page)
- Reuse localStorage cart functions from `carrito.html`
- Accessibility: focus trap, Escape, ARIA `role="dialog"`, `prefers-reduced-motion`

### Out of Scope
- Server-side cart (stays localStorage)
- Cart overlay on non-listing pages
- Hero changes beyond removing search bar
- Modifications to `carrito.html`

## Capabilities

No new capabilities — pure UI restructure. Existing specs apply: `responsive-layout`, `accessibility-foundations`, `touch-targets`, `readability-baseline`. No spec-level requirements change.

### New Capabilities
None

### Modified Capabilities
None

## Approach

1. Extract search + cart icon from hero into a sticky container (`position: sticky; top: 0; z-index: 50`)
2. Cart click prevented on all sizes → opens overlay from `#dialog-overlay`
3. Overlay renders cart from localStorage using adapted JS (`getCarrito`, `renderizarCarrito`, `enviarPedido`)
4. Bottom-sheet on mobile (<1024px), centered modal on desktop
5. Product cards: replace WhatsApp "Comprar" href with "Agregar al carrito" button

## Affected Areas

| Area | Impact | What |
|------|--------|------|
| `index.html` | Major | Sticky bar, overlay HTML, JS wiring |
| `carrito.html` | Reference | Extract cart functions for overlay reuse |
| `toast.html` | Extended | Reuse dialog-overlay for cart modal |
| `producto-cards.html` | Modified | "Comprar" → "Agregar al carrito" |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Sticky bar z-index conflicts with hero/overlay | Med | Ladder: hero 1, sticky 50, overlay 1000+ |
| Cart logic diverges from carrito.html | Med | Extract shared functions into inline script used by both |
| Desktop overlay contradicts prior nav behavior | Low | Per user decision — overlay on ALL sizes |

## Rollback

Revert `index.html`, `producto-cards.html` via git. Other files are additive only.

## Dependencies

None — all client-side.

## Success Criteria

- [ ] Sticky bar visible when scrolling past hero
- [ ] Cart overlay opens on cart icon tap (all sizes), closes on Escape/backdrop/close
- [ ] Overlay shows correct items, quantities, total from localStorage
- [ ] Quantity changes, remove, vacate work in overlay
- [ ] WhatsApp button sends correct order
- [ ] Product cards add item to localStorage on "Agregar al carrito"
- [ ] Focus trapped in overlay, returned on close
- [ ] `mvnw.cmd test` passes
