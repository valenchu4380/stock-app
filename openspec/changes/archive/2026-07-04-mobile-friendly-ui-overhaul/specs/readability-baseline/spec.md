# Readability Baseline

## Objective

Ensure all text meets WCAG AA font and contrast standards across every template — 16px minimum body text, 4.5:1 text contrast, 3:1 large text contrast — without changing the current color palette.

## Requirements

### Requirement: Body font size ≥ 16px

All body, paragraph, list, and label text SHALL render at a minimum computed font size of 16px on mobile viewports (≤ 768px). Headings SHALL scale proportionally (h1 ≥ 24px, h2 ≥ 20px, h3 ≥ 18px).

#### Scenario: Body text meets minimum size

- GIVEN a mobile viewport (≤ 768px wide)
- WHEN any template page renders body content (`<p>`, `<li>`, `<label>`, `<td>`, `<span>` with text)
- THEN the computed font size SHALL be ≥ 16px

#### Scenario: Headings scale proportionally

- GIVEN a page with headings (`h1`–`h3`)
- WHEN inspected on a mobile viewport
- THEN `h1` SHALL be ≥ 24px, `h2` ≥ 20px, `h3` ≥ 18px

### Requirement: WCAG AA contrast on text

All normal-sized text SHALL meet a minimum contrast ratio of 4.5:1 against its background. Large text (≥ 18px bold or ≥ 24px regular) SHALL meet 3:1 minimum. The current color palette SHALL be preserved — only adjust shade values where needed.

#### Scenario: Normal text contrast passes AA

- GIVEN any text element with font-size < 18px (or < 24px if bold)
- WHEN measured against its background color
- THEN the contrast ratio SHALL be ≥ 4.5:1

#### Scenario: Large text contrast passes AA

- GIVEN any text element with font-size ≥ 18px bold or ≥ 24px regular
- WHEN measured against its background color
- THEN the contrast ratio SHALL be ≥ 3:1

### Requirement: Link contrast distinguished from body text

Links that appear within body text SHALL use a color with ≥ 4.5:1 contrast against background AND SHALL be distinguishable from body text by more than color alone (underline or icon).

#### Scenario: Links are distinguishable

- GIVEN a page with inline links
- WHEN the link is inspected
- THEN the link SHALL have an underline or preceding icon AND meet ≥ 4.5:1 contrast

## Verification Criteria

- Manual: inspect each template with DevTools computed styles on mobile viewport (375px)
- Automated: `mvnw.cmd test` — all existing tests pass
- Contrast: measure with DevTools color picker contrast checker on every template

## Affected Templates

All 10 templates + 2 fragments: `templates/*.html`, `fragments/*.html`
