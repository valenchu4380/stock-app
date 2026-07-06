# Exploration: MejorarVisualCodigo — Front-end Code Restructuring

## Current State

The front-end consists of 8 Thymeleaf templates + 2 fragments, 1 shared CSS file, and 1 shared JS file. **There is no architectural separation of concerns.** CSS and JS are mixed inline within HTML templates, duplicated across pages, and inconsistently organized.

### Architecture Overview

```
src/main/resources/
├── templates/
│   ├── fragments/
│   │   ├── producto-cards.html   (41 lines — HTML+JS: card grid fragment)
│   │   └── toast.html            (223 lines — HTML+CSS+JS: toast/dialog system)
│   ├── index.html                (903 lines — HTML+CSS+JS: product listing + cart overlay)
│   ├── carrito.html              (531 lines — HTML+CSS+JS: full-page cart, likely dead)
│   ├── detalle.html              (667 lines — HTML+CSS+JS: product detail + promo)
│   ├── form.html                 (278 lines — HTML+CSS+JS: product create/edit)
│   ├── dashboard.html            (887 lines — HTML+CSS+JS: financial dashboard + Chart.js)
│   ├── compras.html              (125 lines — pure HTML+CSS: orders table)
│   ├── movements.html            (223 lines — pure HTML+CSS: movement history)
│   └── admin-login.html          (107 lines — pure HTML+CSS: login form)
├── static/
│   ├── css/
│   │   └── main.css              (152 lines — shared utilities + CSS custom properties)
│   └── js/
│       └── carrito-compartido.js (150 lines — shared cart functions)
```

### File-by-File Breakdown

#### CSS (`static/css/main.css` — 152 lines)
Contains only **global/shared styles**: CSS custom properties (color palette, touch targets), reset, utilities (`.sr-only`, `.touch-target`, `.focus-visible`, `.btn`, `.skip-link`, `.sticky-bar`, `.no-scroll`), reduced-motion media query.

**What's missing**: Any page-specific styles, component styles, responsive breakpoints (except reduced-motion).

#### JS (`static/js/carrito-compartido.js` — 150 lines)
Contains **shared cart functions**: `getCarrito()`, `guardarCarrito()`, `agregarAlCarrito()`, `cambiarCantidad()`, `eliminarDelCarrito()`, `limpiarCarrito()`, `enviarPedido()`, `actualizarContadorGlobal()`, and the `PROMO` config/`promoActiva()`.

#### `index.html` (903 lines)
- **518 lines of inline CSS** in `<style>` (hero, cart-icon, search-bar, product-grid, product-card, filters, pagination, empty-state, **complete cart overlay** — all with responsive breakpoints at 1024px)
- **~280 lines of inline JS** in `<script>` (cart overlay functions: `abrirCarrito`, `cerrarCarrito`, `renderizarOverlayCarrito`, `actualizarPromoEnOverlay`; search timer; DOMContentLoaded events; infinite scroll)
- **~105 lines of HTML** (hero, sticky-bar with search, filter form, product grid via fragment, empty state, scroll sentinel, cart overlay HTML, toast fragment)
- **PROMO defined inline** in the JS block, duplicating the one in `carrito-compartido.js`
- **subPorCat mapping** defined inline (also exists in `form.html`)

#### `carrito.html` (531 lines — suspected dead code)
- **~280 lines of inline CSS** (hero, container, empty state, cart items, quantity controls, gift banner, countdown)
- **~180 lines of inline JS** (countdown widget, PROMO config duplicated, `renderizarCarrito`, `enviarPedido` override, `limpiarCarrito`, `renderizarCarrito`)
- **~70 lines of HTML** (breadcrumbs, hero, empty state, cart content, gift banner, total)
- **Loads `carrito-compartido.js`** AND then **redefines PROMO** inline, plus `enviarPedido()` which overrides the shared version

#### `detalle.html` (667 lines)
- **~380 lines of inline CSS** (nav-bar, detail card grid layout, image, info, brand badges, price, stock indicators, quantity selector, buttons, gift banner, related products grid, countdown)
- **~150 lines of inline JS** (countdown widget, **PROMO duplicated**, `actualizarPromoBanner`, `getCarrito`/`guardarCarrito` duplicates, `cambiarQty`, `agregarAlCarrito` override)
- **~130 lines of HTML** (nav/breadcrumb, gift banner, detail card, related products grid)
- **getCarrito/guardarCarrito duplicated** from `carrito-compartido.js`

#### `form.html` (278 lines)
- **~130 lines of inline CSS** (hero, form container, form groups, buttons, breadcrumbs, responsive)
- **~50 lines of inline JS** (subPorCat mapping duplicated from index.html, `actualizarSubs`)
- **~100 lines of HTML** (breadcrumbs, hero, form with Thymeleaf fields)

#### `dashboard.html` (887 lines)
- **~210 lines of inline CSS** in `<style>` at top (hero, container, breadcrumbs, back button, stats grid, charts grid, empty section, message, dashboard filters, quick actions cards)
- **~130 lines of inline CSS** in second `<style>` block (`.gestion-section*`, table, stock controls, action buttons)
- **~55 lines of inline CSS** in third `<style>` block (modal overlay, modal content, form groups)
- **~290 lines of inline JS** in two `<script>` blocks (stock adjustment, delete confirmation + **Chart.js chart initialization** with th:inline data)
- **Modal HTML** with its own inline CSS + form
- **Does NOT include the toast fragment** (dashboard doesn't have `<th:block th:insert="~{fragments/toast :: toast}">`)

#### `compras.html` (125 lines)
- **~55 lines of inline CSS** (hero, container, table, badges, responsive card view)
- **0 lines of inline JS**
- **Does NOT include toast fragment**

#### `movements.html` (223 lines)
- **~130 lines of inline CSS** (hero, container, table, badges, pagination, responsive)
- **0 lines of inline JS**
- **Does NOT include toast fragment**

#### `admin-login.html` (107 lines)
- **~80 lines of inline CSS** (centered login card, form controls)
- **0 lines of inline JS**
- **Simplest template**, no fragments used

#### `fragments/producto-cards.html` (41 lines)
- **Pure HTML/Thymeleaf**: card grid fragment. Minimal JS (inline onclick for `agregarAlCarrito` only).
- **Good pattern** — properly extracted reusable component.

#### `fragments/toast.html` (223 lines)
- **HTML + CSS + JS all in one fragment**. CSS (~95 lines for toast + dialog overlay). JS (~105 lines: `showToast`, `trapFocus`, `showConfirm`, `showPrompt`, `agregarCerrarConEscape`).
- **Mixed concern** — contains UI structure, styling, and behavior all in one file.

### CSS Duplication Analysis

The following CSS patterns are repeated across multiple templates:

| Pattern | Templates | Lines of duplication |
|---------|-----------|---------------------|
| `.hero` gradient + white text + padding | index, form, dashboard, carrito, movements, detalle | ~20 lines × 6 = 120 lines |
| `body { background: var(--color-fondo); }` | index, form, dashboard, carrito, detalle, compras, movements | 1 line × 7 files |
| `.breadcrumbs` styling | form, dashboard, compras, movements | ~15 lines × 4 = 60 lines |
| Brand color classes (`.avon`, `.natura`, `.saphirus`) | index (card-brand), detalle (detalle-marca, rel-card-brand) | ~6 lines × 3 appearances |
| Empty state pattern | index, dashboard, carrito | ~20 lines × 3 = 60 lines |
| `.btn` base styles | index, detalle | ~10 lines × 2 |
| Responsive grid collapse at 1024px | index, form, dashboard, carrito, detalle, movements | varying amounts |
| Gift banner styles | detalle, carrito | ~30 lines × 2 |
| Countdown styles | detalle, carrito | ~15 lines × 2 |
| `.text-red`, `.text-green`, `.text-yellow` | index (card-stock-status), detalle, dashboard | ~3 lines × 3 appearances |
| Pagination styles | index, movements | ~25 lines × 2 |
| `.mensaje.err` error box | form, dashboard, detalle | ~5 lines × 3 |
| `.focus-visible` / `.touch-target` class usage (at least the classes are shared) | all interactive templates | 0 — these are utilities in main.css ✓ |

### JS Duplication Analysis

| Pattern | Files | Notes |
|---------|-------|-------|
| `PROMO` object + `promoActiva()` | carrito-compartido.js, index.html, detalle.html, carrito.html | **4 copies** |
| `actualizarCountdown()` | detalle.html, carrito.html | Inline version differs slightly |
| `getCarrito()` / `guardarCarrito()` | carrito-compartido.js, detalle.html | Duplicated in detalle |
| `enviarPedido()` | carrito-compartido.js (overlay version), carrito.html (full-page version) | Different implementations |
| `subPorCat` mapping | index.html, form.html | Identical mapping object |
| `cambiarCantidad()` | carrito-compartido.js (shared), detalle.html, index.html overlay | Multiple variations |
| `agregarAlCarrito()` | carrito-compartido.js, detalle.html | Different implementations (simple vs. with subCategory check) |

### HTML Structure Issues

- **No layout template**: Every page repeats `<head>`, `<meta>`, `<link>`, `<title>` — 7× duplication of the boilerplate
- **No standardized header**: Hero is repeated across 6 pages with different inline styles
- **No standardized breadcrumb**: 5 different implementations (inline styles differ)
- **Inconsistent fragment usage**: `compras.html`, `movements.html`, `admin-login.html` don't include toast fragment
- **carrito.html likely dead code**: The cart is now a floating overlay on index.html; this template is orphaned
- **Dashboard has CSS in 3 different `<style>` blocks** scattered throughout the file body
- **Modal HTML+CSS+JS in dashboard** is inline instead of being a reusable fragment

### Existing Patterns Worth Preserving

1. **CSS custom properties in `:root`** — excellent pattern, well-chosen names
2. **WCAG utilities** (`.sr-only`, `.touch-target`, `.focus-visible`, `--touch-min`) — keep these in main.css
3. **`prefers-reduced-motion`** support — already present in main.css and toast.css
4. **Thymeleaf fragment pattern** — `producto-cards.html` is a well-structured fragment (HTML only)
5. **Cache-busting via timestamp** on static assets (`v=${#dates.createNow().getTime()}`)
6. **Responsive breakpoint** at 1024px — consistent across templates

---

## Pain Points Summary

### Critical
1. **518 lines of cart overlay CSS in index.html** — this is the single biggest offender, duplicated nowhere else but massive
2. **PROMO object defined in 4 places** — guaranteed maintenance nightmare when promo changes
3. **Dashboard CSS scattered across 3 `<style>` blocks** — unmaintainable
4. **carrito.html is dead code** — serves no purpose, confuses developers

### High
5. **No CSS modularization** — 7 templates with 50-500 lines of inline CSS each
6. **~120 lines of `.hero` CSS duplicated across 6 templates**
7. **Chart.js initialization in dashboard** is inline in HTML, mixing concerns
8. **toast.html is a mixed-concern file** — HTML+CSS+JS all together
9. **dashboard.html doesn't include toast fragment** — inconsistency
10. **compras.html and movements.html don't include toast fragment**

### Medium
11. **No Thymeleaf layout dialect** — each page repeats full HTML boilerplate
12. **Breadcrumbs not extracted** — 5+ implementations with slightly different HTML/CSS
13. **Empty state patterns not extracted into fragments**
14. **Modal in dashboard not extracted into a reusable fragment**
15. **Dashboard "gestion" section CSS** is inline rather than in a proper CSS file
16. **subPorCat mapping duplicated** between index.html and form.html
17. **No consistent `<script>` loading pattern** — some templates use `th:src`, some inline `src`, some both

---

## Improvement Opportunities

### 1. CSS Modularization (Extract inline CSS to CSS files)

**Approach**: Move all page-specific and component-specific CSS from inline `<style>` blocks into dedicated CSS files under `static/css/`.

**Proposed structure:**
```
static/css/
├── main.css                  (shared utilities, reset, custom properties — KEEP)
├── components/
│   ├── hero.css              (hero section)
│   ├── breadcrumbs.css       (navigation breadcrumbs)
│   ├── product-card.css      (product cards, card grid)
│   ├── cart-overlay.css      (cart overlay from index.html)
│   ├── empty-state.css       (empty state pattern)
│   ├── toast.css             (toast notifications + dialog — extracted from toast.html)
│   ├── pagination.css        (pagination controls)
│   ├── gift-banner.css       (gift/promo banner)
│   ├── filter-bar.css        (filter controls)
│   └── modal.css             (modal overlay — from dashboard)
├── pages/
│   ├── index.css             (index page specific)
│   ├── form.css              (form page specific)
│   ├── dashboard.css         (dashboard page + gestion table)
│   ├── detalle.css           (product detail + related products)
│   ├── carrito.css           (if keeping full-page cart)
│   ├── compras.css           (orders page)
│   ├── movements.css         (movements page)
│   └── admin-login.css       (login page)
```

**Pros**: Single source of truth for each component, easier maintenance, smaller HTML files, browser caching
**Cons**: More HTTP requests (mitigated by HTTP/2 multiplexing), initial migration effort
**Effort**: High

### 2. JS Modularization (Extract inline JS to JS files)

**Approach**: Move duplicated and page-specific JS from inline `<script>` blocks into dedicated JS files.

**Proposed structure:**
```
static/js/
├── carrito-compartido.js     (shared cart functions — KEEP + enhance)
├── promo.js                  (PROMO config + promoActiva + countdown — single source of truth)
├── cart-ui.js                (cart overlay rendering: abrirCarrito, renderizarOverlayCarrito)
├── dashboard.js              (Chart.js initialization, stock adjustment, confirmación)
├── detalle.js                (detalle page: cambiarQty, agregarAlCarrito detail-version)
├── form.js                   (form subcategory filtering)
├── index.js                  (index page: search handler, infinite scroll, filter toggles)
├── search.js                 (search bar logic)
└── api.js                    (shared fetch helpers)
```

**Critical deduplication targets:**
- `PROMO` + `promoActiva()` → extract to `promo.js`, load everywhere
- `actualizarCountdown()` → extract to `promo.js`
- `subPorCat` → extract to standalone config (or derive from server data)
- `getCarrito`/`guardarCarrito` duplicates → keep only in `carrito-compartido.js`
- `enviarPedido()` → unify overlay and full-page versions

**Effort**: Medium-High

### 3. Thymeleaf Layout Extraction

**Approach**: Use a layout dialect (or manual fragments) to extract the HTML boilerplate shared across all pages.

**Potential patterns:**
- **Layout fragment**: Create `fragments/layout.html` with `<head>`, `<meta>`, CSS links, common header, footer
- **Each template** uses `th:replace="~{fragments/layout :: base}"` and fills content blocks

**Reusable components that could become fragments:**
- `fragments/layout.html` — `<head>` boilerplate + CSS links
- `fragments/hero.html` — hero section with title/subtitle params
- `fragments/breadcrumbs.html` — breadcrumb nav
- `fragments/empty-state.html` — empty state with icon, text, action link
- `fragments/gift-banner.html` — gift promotion banner
- `fragments/modal.html` — reusable modal component
- `fragments/pagination.html` — pagination controls

**Effort**: High (this is a cross-cutting change affecting all templates)

### 4. Carrito.html — Remove or Archive

**Approach**: Since the cart is now a floating overlay on `index.html`, `carrito.html` is dead code. Remove the template AND the `/productos/carrito` controller endpoint.

**Effort**: Low (but coordinate with backend removal)

### 5. Toast Fragment Refactoring

**Approach**: Extract CSS from `toast.html` into `static/css/components/toast.css` and JS into `static/js/toast.js`. Keep `toast.html` as pure HTML fragment only.

**Effort**: Low-Medium

### 6. Dashboard Modal Extraction

**Approach**: Extract the "Actualizar Precios" modal (HTML + CSS + JS) from `dashboard.html` into a reusable `fragments/modal-precios.html`. Extract its CSS into a component CSS file.

**Effort**: Low

---

## Recommended Approach

**Phased approach combining CSS Modularization + JS Deduplication as the primary focus:**

### Phase 1 (Core — highest impact)
1. Extract PROMO to `static/js/promo.js` — eliminate 4 copies, single source of truth
2. Extract cart overlay CSS from `index.html` to `static/css/components/cart-overlay.css`
3. Extract toast CSS+JS from `toast.html` to component files
4. Remove `carrito.html` (dead code)

### Phase 2 (CSS organization)
5. Extract `.hero` to `static/css/components/hero.css`
6. Extract `.breadcrumbs` to `static/css/components/breadcrumbs.css`
7. Extract empty state to component CSS
8. Create page-specific CSS files for large pages (dashboard, index, detalle)
9. Move Dashboard's scattered CSS into a single page file

### Phase 3 (JS cleanup)
10. Extract `subPorCat` to a shared config/JS file
11. Remove `getCarrito`/`guardarCarrito` duplicates from detalle.html
12. Clean up `enviarPedido` — one unified version
13. Add toast fragment to compras.html, movements.html  
14. Extract countdown to promo.js

### Phase 4 (Optional — layout)
15. Create layout fragment for HTML boilerplate
16. Extract reusable components into fragments (hero, breadcrumbs, empty-state, gift-banner)

---

## Risks

- **CSS specificity conflicts** — Moving inline CSS to external files could change cascade order. Inline `<style>` has higher specificity than external `<link>`. Need to audit CSS order carefully.
- **Cache invalidation** — Existing timestamp-based cache-busting (`v=${#dates.createNow().getTime()}`) will cause flickering; consider a version constant that changes only on deployment.
- **Template rendering changes** — Thymeleaf processes fragments; moving CSS out of templates means templates won't render styled if accessed directly without the server. That's actually the goal, but testing is essential.
- **Carrito page removal** — Must verify that no bookmark or external link depends on `/productos/carrito`. The controller endpoint must be removed in sync.
- **Testing gap** — With 1 test file, there's no automated guard against regressions. Manual verification of every template is required.
- **PROMO deduplication** — The PROMO object has slightly different properties in each file (e.g., carrito.html has `endDate` and start logic, detalle.html has different `actualizarPromoBanner`). Need to reconcile differences before consolidating.
- **Dashboard Chart.js** — The dashboard uses `th:inline="javascript"` to pass backend data. This pattern must be preserved when moving Chart.js initialization to an external JS file. Consider using a `<script>` tag with data attributes or a global config.

---

## Ready for Proposal

Yes. The exploration reveals a clear picture of extensive inline CSS/JS duplication and mixed concerns. A phased proposal should cover CSS modularization first (highest impact on readability and separation), followed by JS deduplication.
