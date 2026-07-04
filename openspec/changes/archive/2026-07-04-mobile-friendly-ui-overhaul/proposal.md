# Proposal: Mobile-Friendly UI Overhaul

## Intent

95% mobile traffic from elderly users, but the UI fails WCAG AA on fonts (11.5-15px), touch targets (24-38px), and contrast (4.2:1 text, 2.5:1 links). This change fixes readability and usability for elderly adults on mobile while keeping desktop working. No framework migration.

## Scope

### In Scope
- Base font → 16px minimum, scale headings proportionally
- Touch targets → 44x44px minimum on all interactive elements
- Color contrast → WCAG AA (4.5:1 text, 3:1 large text). Keep current palette.
- Responsive layout → breakpoints 768→1024px, admin table card collapse
- Navigation → breadcrumbs on every page, simplify 7+ filter controls
- Accessibility → ARIA labels, `:focus-visible`, keyboard nav, `prefers-reduced-motion`
- Bug fixes → admin-login encoding (Contraseña), dashboard `brandGanancias` var

### Out of Scope
- CSS framework migration — keep inline CSS per user decision
- PWA / service worker / offline
- Color palette redesign — only improve existing contrast
- Cart persistence change — keep localStorage
- Backend/controller changes
- Skeleton loaders, voice search, full WCAG audit

## Capabilities

### New Capabilities
- `readability-baseline`: Font sizes + color contrast across all templates
- `touch-targets`: 44x44px min sizing for buttons, links, form controls
- `responsive-layout`: Breakpoints (768→1024px), mobile card tables
- `navigation-clarity`: Breadcrumbs, simplified filters, reduced cognitive load
- `accessibility-foundations`: ARIA, focus-visible, keyboard nav, reduced-motion
- `bug-fixes-admin`: Encoding fix (admin-login), undefined var (dashboard)

### Modified Capabilities
None — no existing specs.

## Affected Areas

| File | Changes |
|------|---------|
| `templates/index.html` | Fonts, touch targets, contrast, nav, ARIA, filters, breadcrumbs |
| `templates/detalle.html` | Fonts, touch targets, contrast, ARIA, breadcrumbs |
| `templates/carrito.html` | Fonts, touch targets, contrast, ARIA |
| `templates/form.html` | Fonts, touch targets, contrast, ARIA, form labels |
| `templates/dashboard.html` | Fonts, touch targets, contrast, ARIA, bug fix |
| `templates/compras.html` | Fonts, touch targets, contrast, ARIA, responsive table |
| `templates/lineas.html` | Fonts, touch targets, contrast, ARIA, color scheme |
| `templates/movements.html` | Fonts, touch targets, contrast, ARIA, responsive table |
| `templates/admin-login.html` | Fonts, contrast, ARIA, encoding fix |
| `fragments/producto-cards.html` | Fonts, touch targets, contrast, ARIA |
| `fragments/toast.html` | Fonts, touch targets, contrast, ARIA, focus trap |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Live Railway DB required for tests | High | Tests will run against live DB; no mock fallback |
| No visual regression tests | Med | Manual visual pass across mobile + desktop viewports |
| Toast fragment breaks dialogs | Low | Test all confirm/prompt flows post-change |
| 12 templates = missed file | Low | Systematic checklist; template-by-template pass |

## Rollback Plan

`git revert <commit-hash>`. All changes confined to HTML templates — no DB, config, or backend impact. Independent revert per file if partial rollback needed.

## Success Criteria

- [ ] All body text ≥ 16px on mobile viewports
- [ ] All interactive elements ≥ 44x44px touch target
- [ ] Text contrast ≥ 4.5:1 (AA) normal, ≥ 3:1 large text
- [ ] Admin tables collapse to cards below 1024px
- [ ] Breadcrumbs on every page
- [ ] `:focus-visible` visible on keyboard nav
- [ ] `Contraseña` displays correctly in admin login
- [ ] Dashboard loads without JS console errors
- [ ] All existing tests pass (`mvnw.cmd test`)
