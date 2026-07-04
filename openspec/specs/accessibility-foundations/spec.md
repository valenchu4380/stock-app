# Accessibility Foundations

## Objective

Lay accessibility groundwork across all templates: ARIA labels for interactive elements, `:focus-visible` styles for keyboard navigation, basic keyboard traversal support, and `prefers-reduced-motion` respect for users with vestibular disorders.

## Requirements

### Requirement: ARIA labels on interactive elements

Every button, link, form control, and icon-only element SHALL have an accessible name via `aria-label`, `aria-labelledby`, or visible text content. Icon-only buttons SHALL use `aria-label`.

#### Scenario: Buttons have accessible names

- GIVEN a `<button>` element on any template
- WHEN inspected for accessibility
- THEN the button SHALL have an accessible name (visible text or `aria-label`)

#### Scenario: Icon-only actions are labelled

- GIVEN an icon-only link or button (e.g., edit, delete, close)
- WHEN inspected
- THEN it SHALL have a non-empty `aria-label` describing the action

### Requirement: Focus-visible styles

All interactive elements SHALL have a visible `:focus-visible` outline (minimum 2px offset) when focused via keyboard. The `:focus` style alone SHALL NOT replace `:focus-visible` — mouse clicks SHALL NOT show the outline.

#### Scenario: Keyboard focus is visible

- GIVEN a page with interactive elements
- WHEN navigating via Tab key
- THEN each focused element SHALL display a visible outline (≥ 2px offset)

#### Scenario: Mouse click does not show focus ring

- GIVEN an interactive element
- WHEN clicked with a mouse/pointer
- THEN the element SHALL NOT display the focus-visible ring

### Requirement: Keyboard navigation basics

All interactive elements SHALL be reachable via Tab in logical DOM order. No element SHALL have `tabindex > 0`. Focus SHALL NOT be trapped in the toast dialog unless it is open, in which case focus SHALL cycle within the dialog and return on close.

#### Scenario: Tab order follows visual order

- GIVEN any template
- WHEN navigating with Tab key from top to bottom
- THEN focus SHALL move in a logical, predictable order
- AND no element SHALL require Tab more than N presses to reach (N = visible interactive elements)

#### Scenario: Toast dialog traps focus when open

- GIVEN the toast dialog/confirm dialog is visible
- WHEN pressing Tab repeatedly
- THEN focus SHALL cycle only within the dialog
- AND closing the dialog SHALL return focus to the triggering element

### Requirement: Respect prefers-reduced-motion

All CSS animations, transitions, and JS-driven animated effects SHALL be disabled or reduced when the user's system preference is `prefers-reduced-motion: reduce`. Transitions that affect dimensions or positioning SHALL be removed or set to `none`.

#### Scenario: Animations disabled with reduced motion

- GIVEN a user with `prefers-reduced-motion: reduce` set
- WHEN any template page renders
- THEN no CSS animation, transition, or JS motion effect SHALL play

## Verification Criteria

- Manual: `TAB` through each page — verify focus-visible shows on keyboard nav, hides on click
- Manual: inspect ARIA labels with DevTools Accessibility pane
- Manual: enable prefers-reduced-motion in DevTools Rendering tab — verify no animations
- Manual: test toast confirm/cancel focus trap
- Automated: `mvnw.cmd test` — all existing tests pass

## Affected Templates

All templates and fragments
