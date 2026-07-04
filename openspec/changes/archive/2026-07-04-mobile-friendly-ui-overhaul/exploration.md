# Exploration: mobile-friendly-ui-overhaul

## Current State

The application is a **perfumery e-commerce** (SobreVivi) with **two distinct UI zones**:

### Public Pages (customer-facing, 95% mobile)
| Template | Purpose | Route |
|----------|---------|-------|
| `index.html` | Product listing with search, filters, sort, infinite scroll | `/productos` |
| `detalle.html` | Product detail with cart controls, WhatsApp buy, promo banners | `/productos/detalle/{name}/{sub}` |
| `carrito.html` | Shopping cart (localStorage), WhatsApp checkout | `/productos/carrito` |

### Admin Pages (internal management)
| Template | Purpose | Route |
|----------|---------|-------|
| `form.html` | Product create/edit form | `/productos/nuevo`, `/productos/editar/{name}` |
| `dashboard.html` | Financial dashboard with Chart.js, stock management, product table | `/productos/dashboard` |
| `compras.html` | Orders management | `/productos/compras` |
| `lineas.html` | Line cost configuration | `/productos/lineas` |
| `movements.html` | Movement history | `/productos/movimientos` |
| `admin-login.html` | Admin authentication | `/admin/login` |

### Fragments
| Fragment | Purpose |
|----------|---------|
| `fragments/producto-cards.html` | Card grid rendered by Thymeleaf, used by infinite scroll AJAX |
| `fragments/toast.html` | Toast notifications + confirm/prompt dialogs (shared across all pages) |

---

## CSS Approach

- **No external CSS files** — all styles are inline `<style>` blocks duplicated in every template
- **No CSS framework** (no Bootstrap, Tailwind, or any library)
- Custom design system via CSS custom properties (`:root` blocks repeated in every file):
  - Warm palette: crema (#F5EFE6), melocoton (#EDD5CB), rosa (#C97B7B), verde (#7D9E78)
  - Text colors: #5C4A3A (texto), #3B2E24 (texto-oscuro)
- **Same custom properties declared in every template** — no shared CSS, massive duplication

---

## Mobile Responsiveness Assessment

### Good
- Viewport meta tag present on all pages (`width=device-width, initial-scale=1.0`)
- Responsive breakpoint at `@media (max-width: 768px)` on most pages
- Product grid uses `grid-template-columns: repeat(auto-fill, minmax(240px, 1fr))` — collapses to `repeat(2, 1fr)` on mobile
- Tables in compras.html collapse to card layout on mobile (`display: block` on small screens)
- Form container goes from 50% → 92% width on mobile
- Dashboard charts grid collapses to single column

### Needs Improvement
- **768px breakpoint is too aggressive** — many tablets at 810-1024px get desktop layouts with tiny text
- **No `touch-action` CSS properties** on interactive elements
- Some pages lack responsive tables (movements.html has `overflow-x: auto` but doesn't collapse)
- **Admin pages** (lineas.html, movements.html) use a completely different color scheme (blue #2563eb) — inconsistent with the public UI

---

## Accessibility Issues for Elderly Users

### Font Sizes — CRITICAL
All sizes are too small for elderly users (recommended minimum: 16px body text, 18px for elderly):

| Element | Current Size | Recommended | Issue |
|---------|------------|-------------|-------|
| Body text | 0.85-0.95rem (13-15px) | 1rem+ (16px+) | 🔴 Too small |
| Card titles | 0.95rem (15px) | 1.1rem+ | 🟡 Small |
| Stock status | 0.8rem (12.8px) | 0.95rem+ | 🔴 Very small |
| Filter selects | 0.88rem (14px) | 1rem+ | 🟡 Small |
| Pagination | 0.9rem (14px) | 1rem+ | 🟡 Small |
| Brand badges | 0.72rem (11.5px) | 0.85rem+ | 🔴 Very small |
| Sort buttons | 0.85rem (13.5px) | 1rem+ | 🟡 Small |
| Cart item sub | 0.78rem (12.5px) | 0.9rem+ | 🔴 Very small |
| Dashboard table | 0.88rem (14px) | 1rem+ | 🟡 Small |
| Dashboard th | 0.72rem (11.5px) | 0.85rem+ | 🔴 Very small |
| Dashboard labels | 0.82rem (13px) | 0.95rem+ | 🟡 Small |

### Touch Targets — CRITICAL
WCAG minimum: 44x44px for touch targets. Current state:

| Element | Current Size | Meets 44x44? |
|---------|-------------|--------------|
| Quantity buttons (detalle) | 38x42px | ❌ (42px < 44px) |
| Quantity buttons (dashboard) | 26x24px | ❌🔴 Very small |
| Action buttons (dashboard) | 32x32px | ❌🔴 Very small |
| Cart +/- buttons | 30x30px | ❌🔴 |
| "Comprar" WhatsApp button | 12px padding | ✅ Adequate |
| Sort buttons (filtros) | 8px 16px | 🟡 Borderline |
| Pagination buttons | 8px 14px | 🟡 Borderline |

### Color Contrast — WARNING
- **`--color-texto: #5C4A3A` on `--color-crema: #F5EFE6`**: #5C4A3A (R:92 G:74 B:58) on #F5EFE6 (R:245 G:239 B:230). Luminance ratio approximately 4.2:1 — **fails WCAG AA for normal text** (needs 4.5:1) and **fails for small text** (needs 7:1). Passes AA for large text (18px+).
- **`--color-rosa: #C97B7B` on white**: #C97B7B (R:201 G:123 B:123) on white. Ratio approximately 2.5:1 — **fails WCAG AA for all text sizes**.
- **`--color-verde: #7D9E78` on white**: Ratio approximately 3.2:1 — **fails WCAG AA for normal text**.
- Green/yellow/red stock badges (text on colored backgrounds) may have insufficient contrast.
- **Admin pages** use blue (#2563eb) on white — barely passes at 3.8:1 for normal text.

### Navigation Issues
- **No shared navigation component** — each page has its own nav structure
- No breadcrumbs (except `detalle.html`)
- No "back to top" button for long pages
- Filter controls (search + 2 selects + 3 sort buttons + pagination) = **7+ controls on the main page** — overwhelming for elderly
- No clear visual hierarchy separating search from filters from results
- Admin navigation is text links, not visual cards/buttons
- **No `:focus-visible` styles** on any interactive element

### Form Issues
- No clear error messages next to fields (only flash messages at top)
- No input hint text for elderly users (except URL and linea fields)
- Dropdowns in `form.html` don't have optgroups or visual grouping
- The category/subcategory filtering JS is duplicated between `index.html` and `form.html`
- Form labels use voseo ("Seleccioná") — matches Argentine audience but may confuse less tech-savvy elderly

### Other Issues
- **No ARIA labels** on any interactive element
- **Loading states**: only the WhatsApp button disables; infinite scroll has a spinner text, no skeleton
- **Confirmation dialogs**: present for delete/clear-cart (via toast fragment), but not for form submission
- **Animation preference**: no `prefers-reduced-motion` support (giftPulse animation runs unconditionally)
- **Encoding bug**: `admin-login.html` has `Contraseña` encoded as `Contrase�a` (bad charset/entity)
- **JS bug**: `dashboard.html` line 756 references undefined `brandGanancias` variable (should be `lineaGanancias`)

---

## Patterns and Frameworks

| Aspect | Current State |
|--------|--------------|
| CSS Framework | None — 100% custom inline CSS |
| JavaScript | Vanilla JS, no framework (no Alpine, no Stimulus, no React) |
| Chart library | Chart.js 4.4.7 (CDN) in dashboard only |
| Cart persistence | localStorage (key: `carrito`) |
| Form validation | Server-side only (via `InvalidProductException`) |
| Toast/dialogs | Custom implementation in `toast.html` fragment |
| Infinite scroll | IntersectionObserver API |
| Color scheme | Two inconsistent schemes: warm palette (public) + blue palette (admin pages: lineas, movements) |

---

## Affected Areas

| File | Why affected |
|------|-------------|
| `src/main/resources/templates/index.html` | Main product listing — 95% mobile traffic, most critical page |
| `src/main/resources/templates/detalle.html` | Product detail with cart/promo — second most visited |
| `src/main/resources/templates/carrito.html` | Cart + WhatsApp checkout flow |
| `src/main/resources/templates/fragments/producto-cards.html` | Shared card grid template |
| `src/main/resources/templates/fragments/toast.html` | Shared toast/dialog component |
| `src/main/resources/templates/form.html` | Admin product form |
| `src/main/resources/templates/dashboard.html` | Admin dashboard (financial + stock management) |
| `src/main/resources/templates/compras.html` | Orders management |
| `src/main/resources/templates/lineas.html` | Line cost management |
| `src/main/resources/templates/movements.html` | Movement history |
| `src/main/resources/templates/admin-login.html` | Admin login |

---

## Quick Wins vs Complex Changes

### Quick Wins (Low effort, high impact)
1. **Increase base font size** from 0.85rem to 1rem (or clamp)
2. **Bump touch targets** to 44x44px minimum (quantity buttons, pagination, action buttons)
3. **Improve color contrast** — darken text colors, increase contrast ratios to pass WCAG AA
4. **Fix encoding bug** in `admin-login.html` (Contraseña)
5. **Fix JS bug** in `dashboard.html` (brandGanancias → lineaGanancias)
6. **Add `touch-action: manipulation`** to all buttons/links to eliminate 300ms tap delay
7. **Add `outline`/`:focus-visible` styles** for keyboard accessibility
8. **Unify color schemes** — remove blue scheme from admin pages, use warm palette everywhere
9. **Add `prefers-reduced-motion`** media query to disable animations
10. **Increase mobile breakpoint** from 768px to 1024px for better tablet support

### Medium Changes
1. **Extract shared CSS** — create `static/css/main.css` with custom properties and common styles
2. **Simplify navigation** — reduce filter complexity, add breadcrumbs to all pages
3. **Improve form UX** — inline validation, larger inputs, clearer labels
4. **Add loading states** — skeleton loaders for product grid, button loading spinners
5. **Add ARIA labels** and semantic HTML improvements
6. **Responsive tables** — collapse all admin tables to card layout on mobile
7. **Improve search UX** — debounced search with visual feedback, clear button, recent searches

### Complex Changes
1. **Migrate to CSS framework** (Tailwind or Bootstrap) for consistent design system
2. **Rebuild navigation** with bottom tab bar for mobile (like native app)
3. **PWA features** — service worker, offline fallback, app manifest for "add to home screen"
4. **Voice search** for elderly users with difficulty typing
5. **Cart persistence + sync** — move from localStorage to backend or IndexedDB
6. **Full accessibility audit** + WCAG 2.1 AA compliance

---

## Approaches

### Approach A: Evolutionary — Improve existing CSS inline (Recommended)
Keep the existing inline CSS approach but systematically fix all accessibility issues across all templates. Extract shared CSS custom properties to a single file but keep component styles inline.

- **Pros**: Low risk, no build tooling changes, can ship incrementally, no new dependencies
- **Cons**: Still duplicates some CSS, doesn't solve the architectural CSS problem long-term
- **Effort**: Medium

### Approach B: Framework Migration — Add Tailwind CSS
Introduce Tailwind CSS (or Bootstrap 5) as the CSS foundation. Rewrite all templates to use utility classes.

- **Pros**: Consistent design system, faster future development, accessibility-oriented utilities
- **Cons**: High effort, requires build tooling (npm/postcss), risk of breaking existing UI, longer timeline
- **Effort**: High

### Approach C: Hybrid — Shared design tokens + progressive enhancement
Extract CSS custom properties to a shared file, add a lightweight CSS library (e.g., Bootstrap reboot or modern-normalize), and systematically retrofit accessibility improvements.

- **Pros**: Balanced effort/impact, reusable tokens, lower risk than full migration
- **Cons**: Two CSS systems coexist temporarily, less consistent than full framework
- **Effort**: Medium-High

---

## Recommendation

**Approach A (Evolutionary)** for the initial pass — it's the fastest path to shipping improvements for elderly mobile users. The 95% mobile user base with elderly demographic means we should prioritize readability and touch targets over architectural purity.

Combine with these priorities for the proposal:
1. **Phase 1**: Font sizes, touch targets, contrast — the "readability" pass
2. **Phase 2**: Navigation simplification, form UX, shared CSS extraction
3. **Phase 3**: Framework migration (if warranted) as a separate change

---

## Risks

- **No existing CSS build pipeline** — introducing any framework adds npm/webpack/etc overhead to a simple Maven project
- **Elderly users may be on very old Android devices** — test on actual low-end hardware, not just Chrome DevTools mobile emulation
- **Cart uses localStorage** — no data loss risk during UI changes but any JS refactoring could break cart functionality
- **Inline CSS changes are tedious** — every template must be edited individually; risk of missing one
- **The `toast.html` fragment has the dialog overlay** — breaking the shared fragment breaks confirmations on every page
- **No automated visual regression tests** — manual testing needed after CSS changes
- **Admin pages use different color scheme** — unifying them may confuse admins who are used to the blue palette

## Ready for Proposal

Yes. The exploration is complete. Proceed to `sdd-propose` with the findings above.

Key message for the proposal: **95% mobile, elderly users — prioritize readability and touch targets over architectural improvements.** Phase the work to ship quick wins first.
