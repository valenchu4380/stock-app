# Touch Targets

## Objective

Guarantee every interactive element — buttons, links, form controls, icon-only actions — has a minimum 44×44px touch target on mobile viewports, meeting WCAG 2.2 Target Size (AA) requirements.

## Requirements

### Requirement: Minimum touch target size

Every interactive element SHALL have a touch target of at least 44×44 CSS pixels on mobile viewports (≤ 768px). This includes the element's rendered size OR its padding/clickable area if clipped by overflow.

#### Scenario: Buttons meet 44×44

- GIVEN any `<button>` or element with `role="button"` on a mobile viewport
- WHEN the element's bounding box is measured
- THEN both width and height SHALL be ≥ 44px

#### Scenario: Form controls meet 44×44

- GIVEN any `<input>`, `<select>`, `<textarea>`, `<a>` with interactive role
- WHEN the element's bounding box is measured
- THEN the touch target area SHALL be ≥ 44×44px

#### Scenario: Icon-only actions include sufficient padding

- GIVEN an icon-only button or link (no visible text label)
- WHEN the element's padding is inspected
- THEN padding (top/bottom + content height) SHALL total ≥ 44px in each dimension

### Requirement: Dense control groups maintain spacing

When multiple interactive elements appear in a row (filter bar, nav links, table action icons), the gap between touch targets SHALL be ≥ 4px to prevent adjacent-target mis-taps.

#### Scenario: Adjacent targets have spacing

- GIVEN two adjacent interactive elements on a mobile viewport
- WHEN the gap between their bounding boxes is measured
- THEN the gap SHALL be ≥ 4px

## Verification Criteria

- Manual: inspect each template at 375px viewport, measure bounding boxes in DevTools
- Automated: `mvnw.cmd test` — all existing tests pass

## Affected Templates

`templates/index.html`, `templates/detalle.html`, `templates/carrito.html`, `templates/form.html`, `templates/dashboard.html`, `templates/compras.html`, `templates/lineas.html`, `templates/movements.html`, `fragments/producto-cards.html`, `fragments/toast.html`
