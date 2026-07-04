# Tasks: Update Carrito Search Bar

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 120–200 |
| 400-line budget risk | Low |
| Chained PRs recommended | No |
| Suggested split | Single PR |
| Delivery strategy | ask-on-risk |
| Chain strategy | pending |

Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: pending
400-line budget risk: Low

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Full change — 6 files | PR 1 | Single PR, well under 400 lines |

## Phase 1: Foundation

- [x] 1.1 Create `static/js/carrito-compartido.js` — extract `getCarrito`, `guardarCarrito`, `agregarAlCarrito`, `cambiarCantidad`, `eliminarDelCarrito`, `limpiarCarrito`, `actualizarContadorGlobal`, `enviarPedido`
- [x] 1.2 Export shared functions on `window` object for cross-template access

## Phase 2: Core

- [x] 2.1 Add `.sticky-bar` utility to `static/css/main.css` (`position: sticky; top: 0; z-index: 50`)
- [x] 2.2 Add sticky container to `templates/index.html` with search input + cart icon; prevent cart icon default navigation
- [x] 2.3 Add cart overlay HTML to `templates/index.html` (items list, total, WhatsApp button, vaciar, empty state)
- [x] 2.4 Add cart overlay CSS to `templates/index.html` `<style>`: bottom-sheet (≤1024px), centered modal (>1024px), z-index above sticky bar

## Phase 3: Integration

- [x] 3.1 Wire cart icon click to open overlay and render from localStorage; import `carrito-compartido.js` in `index.html`
- [x] 3.2 Replace WhatsApp "Comprar" link in `templates/fragments/producto-cards.html` with "Agregar al carrito" `<button>` using shared `agregarAlCarrito`
- [x] 3.3 Import `carrito-compartido.js` in `templates/carrito.html`; remove duplicated function defs; keep page-specific countdown/gift-banner/render logic
- [x] 3.4 Add `cerrarConEscape` helper to `templates/fragments/toast.html`

## Phase 4: Accessibility & Polish

- [x] 4.1 Reuse `trapFocus()` from toast.html in overlay; return focus to cart icon on close
- [x] 4.2 Add ARIA: `role="dialog"`, `aria-modal="true"` on overlay, `aria-label` with cart item count on cart icon
- [x] 4.3 Add Escape key handler to close overlay; respect `prefers-reduced-motion` on overlay open/close transitions
