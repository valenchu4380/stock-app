# Delta for product-listing

## MODIFIED Requirements

### Requirement: Sticky Search Bar

The search bar MUST be positioned after the hero/banner and MUST remain visible while scrolling the product list. The search bar MUST use `var(--color-fondo)` as its background so content scrolling behind it is never visible through it.
(Previously: positioned at page top, no background specified)

#### Scenario: Positioned below hero

- GIVEN the page loads
- THEN the search bar MUST appear after the hero/banner
- AND the search bar MUST appear above the filters section

#### Scenario: Sticks on scroll with background

- GIVEN products extend below the fold
- WHEN the user scrolls past the filters section
- THEN the search bar MUST stick at `top: 0` with `z-index: 50`
- AND cards MUST scroll beneath without overlap
- AND `background` MUST be `var(--color-fondo)`
- AND content MUST NOT be visible through the search bar

### Requirement: Cart Overlay Instead of Navigation

The cart icon MUST open an overlay on ALL sizes. No navigation to `/productos/carrito` from the listing. When the overlay is open, the body MUST NOT scroll.
(Previously: overlay behavior only, no scroll lock)

#### Scenario: Opens on mobile (≤1024px)

- GIVEN viewport ≤1024px
- WHEN the cart icon is tapped
- THEN navigation MUST be prevented
- AND the overlay MUST appear as a sliding bottom-sheet

#### Scenario: Opens on desktop (>1024px)

- GIVEN viewport >1024px
- WHEN the cart icon is clicked
- THEN navigation MUST be prevented
- AND the overlay MUST appear as a centered modal

#### Scenario: Body scroll locked when overlay opens

- GIVEN the cart overlay is open
- WHEN the user attempts to scroll the body
- THEN the body MUST NOT scroll
- AND a `.no-scroll` class MUST be applied to `document.documentElement` or `body`

#### Scenario: Scroll position restored on close

- GIVEN the body scroll was locked with saved `scrollY`
- WHEN the overlay closes
- THEN the `.no-scroll` class MUST be removed
- AND the scroll position MUST be restored to its pre-open value

#### Scenario: iOS Safari workaround

- GIVEN the device is iOS Safari
- WHEN the overlay opens
- THEN the body MUST apply `position: fixed; top: -scrollY` in addition to `overflow: hidden`
- AND the body MUST NOT scroll
