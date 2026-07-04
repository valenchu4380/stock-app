# Responsive Layout

## Objective

Improve mobile layout behavior by adjusting breakpoints from 768px to 1024px for tablet collapse, converting admin/data tables to stacked cards on mobile, and ensuring no horizontal scroll or content cutoff below 360px viewport width.

## Requirements

### Requirement: Tablet breakpoint at 1024px

The mobile-first breakpoint SHALL be raised from 768px to 1024px. Layouts SHALL stack/collapse below 1024px and use full-width layout above it.

#### Scenario: Layout collapses at 1024px

- GIVEN a viewport resized from 1200px down to 800px
- WHEN the width passes below 1024px
- THEN the layout SHALL switch from multi-column to stacked/single-column

### Requirement: Admin tables collapse to cards below breakpoint

Data tables in admin/dashboard templates SHALL collapse into card layouts below 1024px: each row becomes a card with headers as left labels. No horizontal scroll SHALL exist inside table containers.

#### Scenario: Table rows become cards on mobile

- GIVEN a `<table>` in `compras.html`, `movements.html`, `lineas.html`, or `dashboard.html`
- WHEN viewed on a viewport < 1024px
- THEN each table row SHALL render as a card with label:value pairs

#### Scenario: No horizontal scroll below breakpoint

- GIVEN any template viewed at 360px viewport width
- WHEN the page is fully rendered
- THEN no horizontal scrollbar SHALL appear (excl. code blocks or pre-formatted text)

### Requirement: Content fits 360px minimum viewport

All templates SHALL render without content cutoff or overflow at 360px viewport width. Elements SHALL use relative units (%, vw) or min-width constraints to avoid fixed-width overflow.

#### Scenario: 360px viewport renders completely

- GIVEN a viewport set to 360×800px
- WHEN each template is loaded
- THEN all content SHALL be visible without horizontal scrolling

## Verification Criteria

- Manual: resize browser to 360px, 768px, 1024px, 1440px — check layout per template
- Manual: verify admin tables transition to cards between 768–1024px
- Automated: `mvnw.cmd test` — all existing tests pass

## Affected Templates

`templates/compras.html`, `templates/movements.html`, `templates/lineas.html`, `templates/dashboard.html`, `templates/index.html`
