## Exploration: update-carrito-search-bar

### Current State

**Product listing page** (`index.html`) — the entire page is a single Thymeleaf template with inline `<style>` and `<script>` blocks. No external JS files exist.

**Search bar:**
- Located inside `<form id="filtroForm">` at line 301-316 of `index.html`
- A simple `<input type="text">` with debounced search (1s timer, line 382-388)
- Positioned below the hero section, NOT sticky — scrolls away with the page
- 80% width centered on desktop, 92% on mobile (media query at line 275)
- The form also contains hidden inputs for page/category/subCategory/sort — the search triggers a full form GET submission

**Cart icon:**
- Inside the hero section (`.hero`), line 296-298 of `index.html`
- CSS: `position: absolute; top: 20px; right: 10%;` with a semi-transparent background
- Links to `/productos/carrito` via `<a class="cart-icon" th:href="@{/productos/carrito}">`
- Displays a count badge from localStorage (`.cart-count #cartCount`)
- On mobile (≤1024px): `position: static;` — sits below hero text, no longer absolute
- Cart count is updated via `actualizarContador()` function (line 418-423)

**Cart page** (`carrito.html`):
- Separate full page at `/productos/carrito` rendering `carrito.html`
- Cart data is stored in `localStorage` under key `'carrito'`
- The page renders items from localStorage, allows quantity changes, clearing, and submitting via WhatsApp
- Order is persisted server-side via `POST /productos/compras/crear` before WhatsApp redirect
- No existing overlay/dropdown for cart — always navigates to this page

**Add-to-cart flow:**
- Only available on the **product detail page** (`detalle.html`) — NOT on the product listing
- Product cards (`producto-cards.html`) have a direct WhatsApp "Comprar" button, not add-to-cart
- The detail page adds items to localStorage via `agregarAlCarrito()` function

**Existing dialog/overlay infrastructure:**
- `fragments/toast.html` has `showConfirm()` and `showPrompt()` functions using a fixed overlay with `#dialog-overlay`
- Uses `display: flex; align-items: center; justify-content: center` — centered modal pattern
- Already handles focus trapping and keyboard navigation

**CSS architecture:**
- `main.css` (111 lines) — shared variables, reset, utilities (`.touch-target`, `.focus-visible`, `.sr-only`, `.btn`)
- Each template has its own `<style>` block for page-specific styles
- No external JS files — all JavaScript is inline in templates

**Responsive behavior:**
- Breakpoint at 1024px (variable `--breakpoint-mobile: 1024px`)
- On mobile: grid goes to 2 columns, cart icon becomes static, filters become column layout
- Cart icon has `position: static` on mobile, no sticky behavior anywhere

### Affected Areas

- `src/main/resources/templates/index.html` — **Primary file**. Contains the search bar (lines 301-316), cart icon (lines 296-298), the inline `<style>` with all positioning (lines 9-287), and the inline `<script>` with cart count / search logic (lines 366-464). Every change touches this file.
- `src/main/resources/templates/carrito.html` — The cart rendering logic (JS at lines 348-541) is the reference for what the overlay needs to display. The `renderizarCarrito()`, `enviarPedido()`, and `actualizarContadorGlobal()` functions would be reused or adapted for the overlay.
- `src/main/resources/static/css/main.css` — Could receive shared sticky/overlay styles if the approach moves away from inline `<style>` blocks.
- `src/main/resources/templates/fragments/toast.html` — The existing overlay infrastructure (`#dialog-overlay`, `showConfirm`/`showPrompt`) could be extended to support the cart overlay pattern.
- `src/main/resources/templates/fragments/producto-cards.html` — Unaffected unless we add add-to-cart buttons to the listing cards.

### Approaches

1. **Sticky search bar — CSS `position: sticky`**
   - Wrap the search bar in a container and apply `position: sticky; top: 0; z-index: 100;`
   - Move the cart icon into the same sticky container (or duplicate it) so both are always visible
   - Add a subtle background/shadow to the sticky bar so content doesn't show through
   - Pros: Pure CSS, no JS overhead, excellent browser support (since 2017), works with the existing form submission pattern
   - Cons: `position: sticky` anchors to its parent container — the search bar is currently inside `<form>` which is a sibling of `.hero`, so it will stick below the hero on scroll; need to ensure z-index doesn't conflict with toast/dialog overlays
   - Effort: Low

2. **Sticky search bar — JS `position: fixed` on scroll**
   - Listen to `scroll` event, then toggle a `.fixed` class on the search bar that applies `position: fixed; top: 0;`
   - Pros: Full control over when it sticks, can add transition effects
   - Cons: Requires JS, scroll listener overhead (need throttling), layout shift when toggling between fixed/static, more fragile
   - Effort: Medium

3. **Cart overlay on mobile — Bottom sheet (slide-up panel)**
   - A div that slides up from the bottom of the viewport when the cart icon is tapped
   - Contains: cart items list (from localStorage), total price, and "Enviar pedido por WhatsApp" button
   - Uses `position: fixed; bottom: 0;` with a transform/translate animation
   - Background overlay (semi-transparent) behind it
   - Pros: Mobile-native feel, natural thumb reach, already similar to the existing `#dialog-overlay` pattern
   - Cons: Needs new HTML structure and CSS animation, needs to handle keyboard/accessibility
   - Effort: Medium

4. **Cart overlay on mobile — Centered modal**
   - Reuse/extend the existing `#dialog-overlay` from `toast.html`
   - Instead of just a confirm/prompt, render cart items inside the dialog
   - Pros: Reuses existing infrastructure, consistent UX with other modals in the app, already handles focus trapping and keyboard navigation
   - Cons: Centered modal on a tall cart list may need scrolling within the modal, less native feel on mobile than a bottom sheet
   - Effort: Medium

5. **Cart overlay on mobile — Dropdown from cart icon**
   - A dropdown/popover that appears directly below the cart icon
   - Pros: Visually anchored to the cart icon, no full-screen overlay
   - Cons: Limited space, hard to position on mobile when cart icon is static, items list may overflow viewport, poor UX for long cart lists
   - Effort: Medium

6. **Keeping cart accessible — Sticky header bar (combined approach)**
   - Create a single sticky container that holds both the search bar and the cart icon
   - On desktop: the bar sticks below the hero background
   - On mobile: the bar sticks at the very top (hero collapses or is behind it)
   - The cart icon inside the bar shows the count and on mobile opens the overlay (no navigation)
   - On desktop, the cart icon could either navigate to `/productos/carrito` or also show overlay
   - Pros: Solves both "sticky search" and "sticky cart" in one structural change, clean consolidated bar
   - Cons: Requires restructuring the hero/serach layout, need to decide desktop behavior (navigate vs overlay)
   - Effort: Medium

7. **Keeping cart accessible — Floating Action Button (FAB)**
   - A circular button fixed at `bottom: 20px; right: 20px;` showing the cart icon + count
   - On tap, opens the cart overlay
   - Pros: Always visible regardless of layout, doesn't interfere with existing hero/header structure, well-understood UX pattern
   - Cons: Adds a new UI element, duplicates the hero cart icon (need to decide if hero icon stays or goes), can overlap content at the bottom of the page
   - Effort: Low

### Recommendation

**Combined approach: Sticky container bar + cart overlay using bottom sheet on mobile**

1. **Restructure the top section** of `index.html`:
   - Keep the hero as-is (brand identity)
   - Move the search bar OUT of the hero into its own container
   - Add the cart icon (or a duplicate) into this new search-bar container
   - Apply `position: sticky; top: 0; z-index: 50;` to this container with a white/crema background and subtle shadow
   - On mobile, the sticky bar stays at top:0 (hero scrolls behind)

2. **Intercept the cart icon click** on mobile:
   - Add a `click` event listener that checks `window.innerWidth <= 1024`
   - If mobile: `preventDefault()` and show the overlay instead of navigating
   - If desktop: allow normal navigation to `/productos/carrito`

3. **Cart overlay as a bottom sheet**:
   - New HTML: a fixed-position panel at the bottom of the viewport
   - Content: cart items (reusing the same rendering logic from `carrito.html`)
   - Shows total and "Enviar pedido por WhatsApp" button
   - Uses a backdrop overlay (can reuse `#dialog-overlay` pattern)
   - Animates up with CSS transform/transition
   - Focus trap inside the sheet (already have `trapFocus()` in toast.html)
   - Close button and backdrop tap to dismiss

4. **Reuse existing cart JS**:
   - The `getCarrito()`, `guardarCarrito()`, `renderizarCarrito()` functions already exist in `carrito.html` — extract the common logic into a shared location or duplicate for the overlay
   - The `enviarPedido()` function that persists the order and redirects to WhatsApp can be reused directly
   - The `actualizarContadorGlobal()` function already exists in `index.html`

This is the most cohesive approach because:
- CSS sticky is the simplest, most performant sticky mechanism
- The bottom sheet pattern is mobile-native and thumb-friendly
- Reusing existing cart JS avoids duplication and maintains consistency
- The FAB is unnecessary since we can make the existing cart icon sticky
- Desktop behavior stays unchanged (navigates to full cart page)

**Effort**: Medium — involves restructuring HTML layout, adding CSS for sticky bar and overlay, and wiring JS to intercept clicks on mobile. No backend changes needed since everything is localStorage-based.

### Risks

- **Layout shift on sticky activation**: When the search bar becomes sticky, it may overlap product cards below. The sticky bar needs a `background` and `z-index` to prevent visual overlap.
- **Hero vs sticky bar z-order**: The hero has `position: relative` and a gradient background. If the sticky bar scrolls over/under the hero, we need to manage z-index carefully. The cleanest approach is to make the sticky bar a sibling that scrolls independently.
- **Cart data freshness**: The overlay reads from localStorage. If the user modifies the cart in another tab or the detail page, the overlay reflects the latest data (since it reads on open). This is already how the cart page works.
- **Form interaction with sticky search**: The search bar is inside a `<form>` that submits on input debounce. Making it sticky should not affect form behavior as the form still wraps it.
- **Infinite scroll interaction**: The product listing uses IntersectionObserver for infinite scroll. The sticky bar must not interfere with the sentinel element at the bottom.
- **Desktop vs mobile cart behavior divergence**: On mobile, the cart icon opens an overlay; on desktop, it navigates to the cart page. This dual behavior could confuse users who resize their browser. Mitigation: use a consistent breakpoint (1024px) and consider always using the overlay approach across all sizes.
- **Accessibility**: The overlay must trap focus when open, close on Escape, and have proper ARIA attributes (role="dialog", aria-modal="true", aria-label). The existing toast.html `trapFocus()` function can be reused.
- **Touch target sizes**: Cart icon and overlay buttons must maintain the 44x44px minimum. The existing `.touch-target` class handles this.

### Ready for Proposal

Yes. The combined sticky container + mobile bottom-sheet overlay approach is well-defined, uses existing infrastructure, and requires no backend changes. The orchestrator should tell the user: "The recommendation is to create a single sticky bar containing the search field and cart icon, and on mobile replace the cart navigation with a bottom-sheet overlay that shows cart contents and a WhatsApp buy button — all using localStorage data and reusing existing cart JS."
