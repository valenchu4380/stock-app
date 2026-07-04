# Design: Sticky Search Bar + Cart Overlay

## Technical Approach

Sticky container bar (search + cart icon) extracted from hero, using `position: sticky`. Cart overlay on all viewport sizes — bottom-sheet on mobile, centered modal on desktop — reusing localStorage cart functions. Product cards switch from WhatsApp direct link to `agregarAlCarrito()`. No backend changes; shared JS file extracted to avoid duplication.

## Architecture Decisions

### Sticky Bar: `position: sticky` vs `position: fixed`

| Option | Tradeoffs | Decision |
|--------|-----------|----------|
| `position: sticky; top: 0` | Pure CSS, no layout shift, anchored to scroll container | **Selected** — container outside hero as independent sibling |
| `position: fixed` + scroll listener | JS overhead, throttling, layout jump when toggling sticky class | Rejected — unnecessary complexity |

### Overlay Rendering: Inline HTML vs Dynamic JS vs Template Fragment

| Option | Tradeoffs | Decision |
|--------|-----------|----------|
| Dynamic JS generation | Same pattern as `carrito.html.renderizarCarrito()`, no server needed | **Selected** — static container, JS fills items from localStorage |
| Thymeleaf fragment | Server round-trip breaks offline-capable cart | Rejected |
| Static pre-rendered HTML | Can't handle variable items | Rejected |

### Cart JS Reuse: Shared File vs Duplicate vs Inline Import

| Option | Tradeoffs | Decision |
|--------|-----------|----------|
| Extract `carrito-compartido.js` | Single source of truth, both pages `<script src>` it | **Selected** — creates `static/js/carrito-compartido.js` |
| Inline duplicate in each template | No HTTP request, but drift risk | Rejected — consistency matters more |
| Inline copy w/ comment sync | Fragile, easy to forget | Rejected |

### Overlay Layout by Viewport

| Option | Tradeoffs | Decision |
|--------|-----------|----------|
| Bottom sheet all sizes | Simple but wastes desktop space | Rejected |
| Centered modal all sizes | Good desktop, poor thumb-reach on mobile | Rejected |
| **Bottom sheet ≤1024px, centered modal >1024px** | Best UX for each form factor | **Selected** |

## Data Flow

```
localStorage ── cart icon click ──→ overlay render
    │                                      │
    ├── agregarAlCarrito()                 │
    │   (product card)                     │
    ├── cambiarCantidad() ─ guardar → re-render
    ├── eliminar / limpiar ─────────→ re-render
    └── enviarPedido() ── POST /productos/compras/crear
                       ──→ wa.me/{num} redirect
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `static/js/carrito-compartido.js` | **Create** | Shared cart: `getCarrito`, `guardarCarrito`, `agregarAlCarrito`, `cambiarCantidad`, `eliminarDelCarrito`, `limpiarCarrito`, `enviarPedido`, `actualizarContadorGlobal` |
| `templates/index.html` | **Modify** | Remove cart icon from hero; add sticky bar (search + cart icon); add overlay HTML, CSS, and JS; import shared JS |
| `templates/fragments/producto-cards.html` | **Modify** | Replace WhatsApp `th:href` with `<button data-* onclick="agregarAlCarrito(this)">`, relabel to "Agregar al carrito" |
| `templates/carrito.html` | **Modify** | Import shared JS, remove duplicated function defs, keep only page-specific rendering |
| `templates/fragments/toast.html` | **Modify** | Add `cerrarConEscape` helper to existing `trapFocus`; confirm z-index 10000 doesn't conflict with sticky bar 50 |
| `static/css/main.css` | **Modify** | Add `.sticky-bar` utility class as optional (overlay CSS stays inline per existing pattern) |

### Precise index.html Changes

1. **Hero (lines 290-299)**: Remove `.cart-icon` anchor — will move to sticky bar.

2. **Sticky bar (insert between lines 299-300)**:
   - New `<div class="sticky-bar">` wrapping the `<form>` + cart icon
   - Form: `display: contents` so search bar and hidden inputs remain in a single form
   - Cart icon: `<a>` with `href="#"`, `id="cartIcon"`, same visual but positioned in bar

3. **Cart overlay HTML (insert before `</body>`, line 465)**:
   - `<div id="cartOverlay" role="dialog" aria-modal="true">`
   - Backdrop div, bottom-sheet/centered panel with header (title + close), scrollable body for items, footer with total + actions

4. **Overlay CSS (~30 lines in `<style>` block)**:
   - Desktop: `position: fixed; inset: 0; align-items: center;` — centered modal
   - Mobile (≤1024px): `align-items: flex-end; border-radius: 16px 16px 0 0;` — bottom sheet
   - Animation: `transform: translateY(0)`, with `prefers-reduced-motion` guard (reuse toast.html pattern)

5. **Script additions (in existing `<script>` block, line 366)**:
   - Import `carrito-compartido.js` via `<script src="...">` at end of `<body>`
   - `abrirCarrito()` / `cerrarCarrito()` — toggle overlay, trap/return focus
   - `renderizarCarritoOverlay()` — renders items into overlay body (adapted from carrito.html)
   - Cart icon click handler: `e.preventDefault(); abrirCarrito();`
   - Escape key + backdrop click → close
   - Replace inline `getCarrito()` / `actualizarContador()` with calls to shared versions

### Precise producto-cards.html Changes

Replace lines 29-33 (`<a>` WhatsApp link):

```html
<button class="btn-comprar touch-target focus-visible"
        th:data-name="${p.name}"
        th:data-price="${#numbers.formatDecimal(p.price, 0, 'COMMA', 2, 'POINT')}"
        th:data-stock="${p.stock}"
        th:data-sub="${p.subCategory}"
        th:data-imagen="${p.imagen != null ? p.imagen : ''}"
        onclick="agregarAlCarrito(this); event.stopPropagation();"
        th:aria-label="|Agregar ${p.name} al carrito|">
  Agregar al carrito
</button>
```

## Interfaces / Contracts

### Shared JS (`carrito-compartido.js`)

```javascript
// Types
// CartItem: { name: string, price: number, cantidad: number, stock: number, subCategory: string, imagen: string }

// Functions
getCarrito()                  → CartItem[]
guardarCarrito(items)         → void
agregarAlCarrito(name, price, stock, subCategory, imagen) → void
cambiarCantidad(index, delta) → void
eliminarDelCarrito(index)     → void
limpiarCarrito()              → void
enviarPedido()                → void  // POSTs then redirects to WhatsApp
actualizarContadorGlobal()    → void  // updates #cartCount in any page
```

### Overlay Events

| Event | Action |
|-------|--------|
| Click cart icon | `e.preventDefault(); abrirCarrito();` |
| Click backdrop | `cerrarCarrito();` |
| Escape key | `cerrarCarrito();` |
| Focus trap | Reuse `trapFocus(overlay)` from toast.html |
| Return focus | Focus restored to cart icon on close |

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | `carrito-compartido.js` cart CRUD | JUnit cannot test JS; manual or Playwright if added later |
| Integration | `/productos/compras/crear` still works | Existing backend tests (POST endpoint) — no behavior change |
| E2E | Full flow: add to cart → open overlay → change qty → WhatsApp | Manual — visual + localStorage inspection |
| Visual | Sticky bar on scroll, overlay transitions, bottom-sheet vs modal | Manual responsive testing |
| Accessibility | Focus trap, Escape close, ARIA attributes, reduced motion | Manual keyboard + screen reader |

No automated JS tests exist in the project. The design follows the existing inline-JS-without-tests pattern. Manual verification per spec scenarios is the pragmatic path.

## Migration / Rollout

No migration required — all data stays in localStorage (existing key `carrito`). The `carrito.html` page remains intact and continues to work. If rollback needed, revert `index.html`, `producto-cards.html`, and delete `carrito-compartido.js`.

## Open Questions

- [ ] Should `carrito.html` eventually redirect to the product listing with overlay open, or remain as standalone fallback? (Deferred — out of scope per proposal)
