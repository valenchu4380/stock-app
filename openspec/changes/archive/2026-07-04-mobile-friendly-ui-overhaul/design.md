# Design: Mobile-Friendly UI Overhaul

## Technical Approach

Evolutionary inline-CSS retrofit (Approach A from exploration). Extract shared CSS custom properties and utility classes to `static/css/main.css` but keep component-specific styles inline per template. Target WCAG AA for font sizes, touch targets, and contrast across all 12 templates. No framework, no build tooling.

## Architecture Decisions

### Decision: Shared CSS file vs. inline-only

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Keep 100% inline | Duplication × 12 files, impossible to maintain contrast palette centrally | ❌ Rejected |
| Extract shared `main.css` | Single source of truth for `:root` variables + utility classes; templates keep component-specific styles inline | ✅ Chosen |

**Rationale**: 11 of 12 templates duplicate identical `:root` blocks. Moving variables + shared utilities to `/static/css/main.css?v={cachebuster}` eliminates that duplication and lets us fix contrast in one place. Component-specific styles stay inline per template (no migration risk).

### Decision: Breakpoint change 768px → 1024px

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Keep 768px | iPads (810-834px) get desktop layouts → tiny text on 95% mobile audience | ❌ Rejected |
| Change to 1024px | Small desktops (1024-1280) get mobile layout on wide screens — acceptable for this user base | ✅ Chosen |

### Decision: Color contrast — darken existing palette, do not add new colors

**Choice**: Darken `--color-texto` from `#5C4A3A` to `#3B2E24` (same as `--color-texto-oscuro`), darken `--color-rosa` from `#C97B7B` to `#A85555` for UI elements on light backgrounds. Keep `--color-verde` at `#7D9E78` (passes AA at 16px+ on white at 3.6:1 — borderline, but only used for badges). Add `--color-rosa-dark: #A85555` and `--color-texto-dark: #3B2E24` as overrides.

### Decision: Admin blue palette → warm palette unification

**Choice**: Replace blue scheme (`#2563eb`, `#1e40af`, `#eff6ff`) in `movements.html` and `lineas.html` with the warm palette variables. Keeps UI consistent across all pages.

## Data Flow

```
Browser ──→ GET /productos* ──→ Controller ──→ Model ──→ Thymeleaf template ──→ HTML with <link href=/css/main.css>
                                                                                        └── inline <style> per component
```

No backend changes. All modifications are HTML/CSS template-only.

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `src/main/resources/static/css/main.css` | Create | Shared `:root` variables (darkened colors), breakpoint mixins, `.btn`, `.touch-target`, `.focus-visible`, `.sr-only`, `.skip-link` utilities, `prefers-reduced-motion` keyframes |
| `templates/index.html` | Modify | Font sizes, touch targets, contrast, ARIA, breadcrumbs, filter simplification |
| `templates/detalle.html` | Modify | Font sizes, touch targets, contrast, ARIA, breadcrumbs |
| `templates/carrito.html` | Modify | Font sizes, touch targets, contrast, ARIA |
| `templates/form.html` | Modify | Font sizes, touch targets, contrast, ARIA, label fixes |
| `templates/dashboard.html` | Modify | Font sizes, touch targets, contrast, ARIA, bug fix: `brandGanancias`→`lineaGanancias` |
| `templates/compras.html` | Modify | Font sizes, touch targets, contrast, ARIA, responsive table to 1024px |
| `templates/lineas.html` | Modify | Font sizes, touch targets, contrast, ARIA, color scheme unify |
| `templates/movements.html` | Modify | Font sizes, touch targets, contrast, ARIA, responsive table, color scheme unify |
| `templates/admin-login.html` | Modify | Font sizes, contrast, ARIA, encoding fix |
| `fragments/producto-cards.html` | Modify | Font sizes, touch targets, contrast, ARIA |
| `fragments/toast.html` | Modify | Font sizes, touch targets, contrast, ARIA, focus trap |

## Interfaces / Contracts

**CSS Custom Properties** (published in `main.css`):

```css
:root {
  --color-fondo: #F9F3EC;
  --color-crema: #F5EFE6;
  --color-melocoton: #EDD5CB;
  --color-rosa-claro: #E8C4C4;
  --color-rosa-borde: #D4B8AE;
  --color-rosa: #A85555;          /* darkened from #C97B7B for AA */
  --color-rosa-hover: #8F4747;
  --color-verde-claro: #A8C4A2;
  --color-verde: #7D9E78;
  --color-texto: #4A3D32;         /* darkened from #5C4A3A for AA */
  --color-texto-oscuro: #3B2E24;
  --font-base: 1rem;              /* 16px base */
  --touch-min: 44px;              /* WCAG minimum touch target */
  --radius: 10px;
  --breakpoint-mobile: 1024px;    /* updated from 768px */
}

.sr-only { /* screen-reader-only */ }
.focus-visible:focus-visible { outline: 3px solid var(--color-rosa); outline-offset: 2px; }
.touch-target { min-height: var(--touch-min); min-width: var(--touch-min); }
@media (prefers-reduced-motion: reduce) { *, *::before, *::after { animation-duration: 0.01ms !important; } }
```

Each template adds `<link rel="stylesheet" th:href="@{/css/main.css(v=${#dates.createNow().getTime()})}">` and removes its duplicate `:root` block.

## Template-by-Template Design

### index.html
- Remove duplicate `:root`, add `<link>` to main.css
- Body text → `1rem` (was 0.85-0.95rem), card title → `1.05rem` (was 0.95rem)
- Brand badges → `0.82rem` (was 0.72rem)
- Stock status → `0.88rem` (was 0.8rem)
- `touch-target` class on all `.btn-comprar`, `.pag-btn`, `.btn-orden`, `.cart-icon`
- `.focus-visible` on all interactive elements
- Add `role="navigation"` + `aria-label="Filtros"` to filter bar
- Add breadcrumb nav bar above product grid: `Inicio > Productos`
- Simplify filter visual grouping: wrap selects + sort in a `<fieldset>` with `<legend class="sr-only">`
- Change breakpoint from 768px to 1024px
- Add `prefers-reduced-motion` to `.giftPulse` animation via shared CSS

### detalle.html
- Remove `:root`, add `<link>` to main.css, remove duplicate `.gift-banner`, `.promo-countdown`, `.rel-card` styles already in main.css
- `.qty-selector button` → min 44×44px (was 38×42)
- `.btn` → min 44px height
- Product name → `1.3rem` (was 1.4rem — ok, keep), price → `1.8rem` (was 2rem)
- Add ARIA: `aria-label="Cantidad"` on qty buttons, `role="img"` on placeholder emoji
- Breadcrumb: already present, add `aria-label="Breadcrumb"`

### carrito.html
- Remove `:root`, add `<link>` to main.css
- `.qty-btn` → 44×44px (was 30×30)
- `.btn-limpiar`, `.btn-whatsapp-enviar` → 44px min-height
- Cart item price → `1rem` (was 1rem — ok), item name → `1rem` (was 0.95rem)
- Add ARIA: `aria-label="Aumentar cantidad"`, `aria-label="Disminuir cantidad"`, `aria-live="polite"` on cart total
- `.btn-volver` → add `touch-target` class

### form.html
- Remove `:root`, add `<link>` to main.css
- Input padding → 14px (was 12px) for easier tapping
- All labels already properly associated via `for`/`id` — add `aria-required="true"` on required fields
- `.btn-guardar`, `.btn-cancelar` → 44px min-height
- Hint text → `0.85rem` (was 0.78rem)

### dashboard.html
- Remove `:root`, add `<link>` to main.css
- Bug fix: line 756 `brandGanancias` → `lineaGanancias`
- `.gestion-stock-btn` → 44×44px (was 26×24 — critical)
- `.gestion-btn` → 44×44px (was 32×32 — critical)
- Table → card collapse at 1024px breakpoint
- Stat numbers → `1.6rem` (was 1.8rem — ok), labels → `0.9rem` (was 0.82rem)
- Add ARIA: `aria-label="Cerrar"` on modal close, `role="dialog"` on modal

### compras.html
- Remove `:root`, add `<link>` to main.css
- Table → card collapse at 1024px (already has mobile card layout at 768px — update breakpoint)
- `.btn-accion` → 44px min-height
- Font sizes → bump all by 1-2px

### lineas.html
- Remove all styles, replace with `<link>` to main.css + warm palette variables
- Unify color scheme: blue `#2563eb` → `var(--color-rosa)`
- `.costo-input` → 44px height, font 1rem
- `.btn-actualizar` → 44px min-height

### movements.html
- Remove all styles, replace with `<link>` to main.css + warm palette variables
- Unify color scheme: blue `#2563eb` → `var(--color-rosa)`
- Table → card collapse at 1024px
- `.badge-action` → font bump to 0.85rem (was 0.78rem)
- Pagination buttons → `touch-target` class

### admin-login.html
- Encoding fix: replace `Contrase�a` with `Contraseña` (UTF-8 encoded)
- Remove `:root`, add `<link>` to main.css
- `.login-btn` → 44px min-height already (14px padding ≈ 48px — ok)
- Add ARIA: `aria-label="Contraseña"` on password input (already has placeholder — add explicit label)
- `? Volver` → change to `← Volver` for consistency

### producto-cards.html (fragment)
- Remove inline `:root` (none exists — inherits from parent page)
- Font bumps: `.card-title` → `1rem` (was 0.95rem), `.card-brand` → 0.8rem (was 0.72rem), `.card-stock-status` → 0.85rem (was 0.8rem)
- `.btn-comprar` already adequate — keep
- Add ARIA: `aria-label` on Comprar link, `role="article"` on each card

### toast.html (fragment)
- Remove duplicate dialog styles (keep minimal, reference shared)
- Dialog button fonts → `1rem` (was 0.95rem)
- Add focus trap: focus first interactive element on dialog open, trap Tab cycling
- Dialog overlay: `role="alertdialog"` + `aria-modal="true"` + `aria-labelledby="dialog-message"`

## Components Design

### Navigation/Breadcrumbs
- Pattern: `<nav aria-label="Breadcrumb"><ol><li><a>…</a></li><li aria-current="page">…</li></ol></nav>`
- Add breadcrumbs to: index.html (minimal: Inicio > Productos), detalle.html (improve existing), admin pages (Dashboard > Sección)
- Each page gets a "back" link (already present on most)

### Button/Link Sizing
- Utility class `.touch-target`: `min-height: 44px; min-width: 44px; padding: 12px 20px;`
- Applied to: pagination, sort, quantity controls, action buttons, cart controls
- `.btn` base: `display: inline-flex; align-items: center; justify-content: center; gap: 8px;`

### Filter Simplification
- Group in `<fieldset>` with `<legend class="sr-only">Filtros</legend>`
- Move sort buttons visually closer to the search-as-you-type input (reduce cognitive distance)
- Keep existing JS behavior (no functional change)

### Focus Indicator
- `.focus-visible:focus-visible { outline: 3px solid var(--color-rosa); outline-offset: 2px; border-radius: var(--radius); }`
- Apply to: all `<a>`, `<button>`, `<input>`, `<select>` elements
- Note: `outline` does not affect layout (safer than `box-shadow`)

### ARIA Annotation Pattern
| Element | ARIA |
|---------|------|
| Navigation | `role="navigation"` / `<nav>` + `aria-label` |
| Breadcrumb | `<nav aria-label="Breadcrumb">` + `aria-current="page"` |
| Filter group | `<fieldset>` + `<legend class="sr-only">` |
| Interactive icons | `role="img"` + `aria-label` |
| Dialog | `role="alertdialog"` + `aria-modal="true"` + `aria-labelledby` |
| Live region | `aria-live="polite"` on cart total, toast container |
| Sort controls | `aria-sort` on table headers (where applicable) |

## Testing Strategy

| Layer | What | Approach |
|-------|------|----------|
| Manual | All pages, mobile + desktop | Chrome DevTools responsive mode at 360px, 768px, 1024px, 1920px |
| Manual | Touch targets | DevTools element inspection: verify ≥44px on all interactive |
| Manual | Contrast | Use browser DevTools contrast checker on body text, links, badges |
| Manual | Keyboard nav | Tab through every page, verify focus-visible visible on each stop |
| Manual | Bugs | Verify "Contraseña" displays correctly, dashboard console has no errors |
| Automated | Existing Java tests | `mvnw.cmd test` — must still pass |

No automated visual regression or accessibility tests exist for this project. Manual checklist is the testing strategy.

## Migration / Rollout

No migration required. All changes are template-only. Single PR with all changes (under 400-line budget risk — touch targets and font bumps are repetitive but the templates are large; consider chained PR if >400 lines).

## Bug Fixes

### admin-login.html encoding (line 105, 112)
- **Root cause**: Non-breaking space (U+00A0) or bad charset interpretation in `Contraseña`
- **Fix**: Rewrite the misencoded character as proper UTF-8 `ñ` (0xC3 0xB1)
- **Also**: Replace `Gesti�n` on line 105 if similarly affected

### dashboard.html `brandGanancias` (line 756)
- **Root cause**: Copy-paste error — `brandGanancias` used instead of `lineaGanancias` in chart dataset
- **Fix**: `s/brandGanancias/lineaGanancias/g` on line 756 and 757
- **Note**: `brandGanancias` is never declared in any scope

## Open Questions

- [ ] Does the shared `main.css` need a version/cache-busting query param? (Proposal: use `v=${#dates.createNow().getTime()}` same as favicon)
- [ ] Admin users have been using the blue scheme in movements/lineas — will the warm palette change disorient them? (Mitigation: keep blue as secondary option, or ship and get feedback)
