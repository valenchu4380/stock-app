# Navigation Clarity

## Objective

Reduce cognitive load for elderly users by adding breadcrumbs to every page, simplifying filter controls from 7+ to essential fields, and establishing a predictable page-to-page navigation structure.

## Requirements

### Requirement: Breadcrumbs on every page

Every page SHALL display a breadcrumb trail at the top of the content area (below the main nav) showing the current page in context of the site hierarchy. The breadcrumb SHALL be a `<nav>` with `aria-label="Breadcrumb"` and the last item SHALL be the current page (not a link).

#### Scenario: Breadcrumbs render on all templates

- GIVEN any template page (index, detalle, carrito, form, dashboard, compras, lineas, movements, admin-login)
- WHEN the page loads
- THEN a `<nav aria-label="Breadcrumb">` element SHALL be present
- AND the final breadcrumb item SHALL be the current page name (non-linked)

#### Scenario: Breadcrumb links navigate correctly

- GIVEN a breadcrumb link (not the current page)
- WHEN clicked/tapped
- THEN the user SHALL navigate to the expected parent page

### Requirement: Simplified filter controls

Filter/sort controls on list pages SHALL be reduced from 7+ to ≤ 4 essential fields on mobile. Non-essential filters SHALL be hidden behind an "Advanced filters" toggle or removed entirely.

#### Scenario: Mobile shows ≤ 4 filter fields

- GIVEN a mobile viewport (≤ 768px) on a page with filters (index, compras, movements, lineas)
- WHEN the page renders
- THEN at most 4 filter/sort controls SHALL be visible
- AND remaining controls SHALL be behind a toggle (button with "Advanced filters")

#### Scenario: Filter toggle reveals hidden fields

- GIVEN a mobile viewport with the "Advanced filters" toggle closed
- WHEN the user taps the toggle
- THEN the hidden filter controls SHALL become visible
- AND the page SHALL NOT reflow unexpectedly

## Verification Criteria

- Manual: navigate every page — verify breadcrumb trail matches hierarchy
- Manual: on 375px viewport, count visible filter controls (≤ 4)
- Manual: tap "Advanced filters" toggle — verify hidden fields appear
- Automated: `mvnw.cmd test` — all existing tests pass

## Affected Templates

`templates/index.html`, `templates/detalle.html`, `templates/carrito.html`, `templates/form.html`, `templates/dashboard.html`, `templates/compras.html`, `templates/lineas.html`, `templates/movements.html`, `templates/admin-login.html`
