# Tasks: Mobile-Friendly UI Overhaul

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~430-480 |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | PR 1 (Foundation + Bug fixes + admin-login/dashboard ~145 lines) → PR 2 (Admin palette + lineas/movements + form/compras/fragments ~195 lines) → PR 3 (Consumer pages index/detalle/carrito ~110 lines) |
| Delivery strategy | ask-always |
| Chain strategy | pending |

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: pending
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Foundation + admin-login/dashboard retrofits + bug fixes | PR 1 | Base: main. Foundation must ship first; admin-login/dashboard are self-contained |
| 2 | Admin palette (lineas/movements) + form/compras/fragments | PR 2 | Depends on PR 1 through main.css |
| 3 | Consumer pages (index/detalle/carrito) | PR 3 | Depends on PR 1 through main.css |

## Phase 1: Foundation

- [x] **task-001**: Create `src/main/resources/static/css/main.css` — shared `:root` vars (darkened colors, 1024px breakpoint), `.touch-target`, `.focus-visible`, `.sr-only`, `.btn`, `.skip-link`, `prefers-reduced-motion` keyframes. No template changes.
  - **depends_on**: none
  - **files_to_modify**: `src/main/resources/static/css/main.css`
  - **requirements**: readability-baseline R2, responsive-layout R1, accessibility-foundations R2+R4
  - **verification**: file exists with correct vars; `:root` colors match design spec (#4A3D32, #A85555, 1024px breakpoint)
  - **estimated_lines**: 80
  - **risk**: low

## Phase 2: Bug Fixes

- [x] **task-002**: Fix admin-login.html encoding (Gesti�n→Gestión, Contrase�a→Contraseña) and dashboard.html JS var (brandGanancias→lineaGanancias on lines 756-757). No other changes.
  - **depends_on**: none
  - **files_to_modify**: `src/main/resources/templates/admin-login.html`, `src/main/resources/templates/dashboard.html`
  - **requirements**: bug-fixes-admin R1+R2
  - **verification**: admin-login shows "Contraseña" correctly in browser; dashboard console has no ReferenceError; `mvnw.cmd test` passes
  - **estimated_lines**: 5
  - **risk**: low

## Phase 3: Admin Palette Unification + Template Retrofits

- [x] **task-003**: Retrofit lineas.html + movements.html — remove inline styles, add `main.css` link, replace all blue (#2563eb, #1e40af, #eff6ff) with warm palette vars (`--color-rosa`), add breadcrumbs, ARIA, table→card at 1024px, font bumps, touch targets.
  - **depends_on**: task-001
  - **files_to_modify**: `src/main/resources/templates/lineas.html`, `src/main/resources/templates/movements.html`
  - **requirements**: readability-baseline R1-R3, touch-targets R1-R2, responsive-layout R1-R3, navigation-clarity R1, accessibility-foundations R1-R4
  - **verification**: DevTools at 375px — fonts ≥16px, buttons ≥44px, no blue color present, breadcrumbs, table→card at 1024px, reduced-motion works; `mvnw.cmd test` passes
  - **estimated_lines**: 120
  - **risk**: medium

## Phase 4: Consumer Template Retrofits

- [x] **task-004**: Retrofit index.html + detalle.html + carrito.html — remove `:root` block, add `main.css` link, bump body/heading fonts to ≥16px, add `.touch-target` to all buttons/links, breadcrumbs (index: Inicio > Productos; detalle: improve existing), filter simplification on index (≤4 controls, rest behind toggle), ARIA labels, focus-visible, prefers-reduced-motion.
  - **depends_on**: task-001
  - **files_to_modify**: `src/main/resources/templates/index.html`, `src/main/resources/templates/detalle.html`, `src/main/resources/templates/carrito.html`
  - **requirements**: readability-baseline R1-R3, touch-targets R1-R2, responsive-layout R1+R3, navigation-clarity R1+R2, accessibility-foundations R1-R4
  - **verification**: body text ≥16px at 375px; buttons ≥44×44px; breadcrumbs on each page; filters show ≤4 controls on mobile; no horizontal scroll at 360px; ARIA labels present; `mvnw.cmd test` passes
  - **estimated_lines**: 110
  - **risk**: medium

## Phase 5: Admin/Data Template Retrofits

- [x] **task-005**: Retrofit form.html + compras.html + dashboard.html — remove `:root` block, add `main.css` link, input padding →14px, font bumps, touch targets (44px min on buttons/inputs), ARIA (`aria-required`, `role="dialog"` on modal), dashboard table→card at 1024px, breakpoint updates.
  - **depends_on**: task-001
  - **files_to_modify**: `src/main/resources/templates/form.html`, `src/main/resources/templates/compras.html`, `src/main/resources/templates/dashboard.html`
  - **requirements**: readability-baseline R1-R3, touch-targets R1-R2, responsive-layout R1-R3, navigation-clarity R1, accessibility-foundations R1-R4
  - **verification**: form inputs ≥44px; table→card on compras/dashboard at 1024px; no horizontal scroll at 360px; breadcrumbs on dashboard; `mvnw.cmd test` passes
  - **estimated_lines**: 85
  - **risk**: medium

- [x] **task-006**: Retrofit fragments producto-cards.html + toast.html — font bumps, ARIA (`role="article"` on cards, `aria-label` on buttons, `role="alertdialog"` on toast dialog), focus trap in toast, touch targets, prefers-reduced-motion on animations.
  - **depends_on**: task-001
  - **files_to_modify**: `src/main/resources/templates/fragments/producto-cards.html`, `src/main/resources/templates/fragments/toast.html`
  - **requirements**: readability-baseline R1-R3, touch-targets R1-R2, accessibility-foundations R1-R4
  - **verification**: card fonts ≥16px; toast buttons ≥44px; ARIA on card article, dialog; Tab traps within dialog when open; reduced-motion stops animations; `mvnw.cmd test` passes
  - **estimated_lines**: 35
  - **risk**: low

## Phase 6: Verification

- [x] **task-007**: Run full test suite + execute manual checklist — `mvnw.cmd clean compile` and `mvnw.cmd test` pass; manual checklist covering contrast, touch targets, keyboard nav, breadcrumbs, responsive layout at 360/768/1024/1920px, console errors, toast focus trap.
  - **depends_on**: task-002, task-003, task-004, task-005, task-006
  - **files_to_modify**: none (run tests)
  - **requirements**: all
  - **verification**: BUILD/TESTS: OK; all manual checklist items pass
  - **estimated_lines**: 0
  - **risk**: low

## Dependency Graph

```
task-001 (Foundation) ──────┐
                            ├──→ task-003 (Admin palette)
                            ├──→ task-004 (Consumer pages)
                            ├──→ task-005 (Admin/data templates)
                            └──→ task-006 (Fragments)
                                    │
task-002 (Bug fixes) ────────┐     │
                              ├─────┴──→ task-007 (Verification)
```

Tasks 002-006 can proceed in parallel after task-001 and task-002 complete. Task-002 has no dependencies and can run first.

## Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| template `:root` removal breaks if vars referenced in same template before main.css loads | Runtime style flash | Ensure `<link>` is placed before `<style>` removal; test with slow 3G throttling |
| admin palette (blue→warm) disorients existing users | Usability surprise | Ship and get feedback per design; keep blue as secondary option if needed |
| 12 templates modified in parallel branches can conflict | Merge conflicts on shared | Apply sequentially within each PR; chained PR strategy avoids cross-branch conflicts |
| `main.css` cache-busting via `#dates.createNow()` creates uncacheable URLs | Performance | Accept for now — template count is low; can add version file later |
