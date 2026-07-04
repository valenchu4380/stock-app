# Product Listing — Full Specification

## Purpose

Product listing page provides browsable cards with a sticky search bar and a cart overlay that replaces full-page cart navigation on all sizes.

## Requirements

### Requirement: Sticky Search Bar

The search bar MUST be positioned after the hero/banner and MUST remain visible while scrolling the product list. The search bar MUST use `var(--color-fondo)` as its background so content scrolling behind it is never visible through it.

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

### Requirement: Cart Overlay Operations

The overlay MUST render cart contents from `localStorage` with full CRUD.

#### Scenario: Displays current cart

- GIVEN the cart has items in localStorage
- WHEN the overlay opens
- THEN it MUST show name, price, quantity, and subtotal per item
- AND the total MUST be calculated and displayed

#### Scenario: Quantity adjustment

- GIVEN the overlay shows cart items
- WHEN the user changes a quantity
- THEN localStorage MUST update
- AND subtotal and total MUST refresh immediately

#### Scenario: Remove item

- GIVEN an item is displayed in the overlay
- WHEN the user clicks remove
- THEN the item MUST be removed from localStorage
- AND the row MUST disappear

#### Scenario: Vacate cart

- GIVEN the overlay has items
- WHEN the user clicks "Vaciar carrito"
- THEN localStorage MUST clear
- AND the overlay MUST show an empty state

#### Scenario: WhatsApp checkout

- GIVEN the overlay has items
- WHEN the user clicks "Enviar pedido por WhatsApp"
- THEN the system MUST POST to `/productos/compras/crear`
- AND redirect to WhatsApp with the order summary

#### Scenario: Empty cart state

- GIVEN localStorage has no items
- WHEN the overlay opens
- THEN it MUST display an empty-cart message
- AND disable the WhatsApp button

### Requirement: Add to Cart from Product Cards

Product cards MUST provide "Agregar al carrito" that writes to localStorage.

#### Scenario: Add product

- GIVEN a product card is displayed
- WHEN the user clicks "Agregar al carrito"
- THEN the product MUST be added to localStorage
- AND a toast MUST confirm the action
- AND the cart count badge MUST update

#### Scenario: Duplicate increments quantity

- GIVEN the product is already in localStorage
- WHEN the user clicks "Agregar al carrito" again
- THEN the quantity MUST increment (no duplicate entry)

### Requirement: Overlay Dismissal and Accessibility

The overlay MUST be dismissible and WCAG AA compliant.

#### Scenario: Close via backdrop

- GIVEN the overlay is open
- WHEN the user clicks the backdrop
- THEN the overlay MUST close
- AND focus MUST return to the cart icon

#### Scenario: Close via Escape

- GIVEN the overlay is open
- WHEN the user presses Escape
- THEN the overlay MUST close
- AND focus MUST return to the cart icon

#### Scenario: Focus trap

- GIVEN the overlay is open
- WHEN the user presses Tab repeatedly
- THEN focus MUST cycle within overlay elements only

#### Scenario: Reduced motion

- GIVEN `prefers-reduced-motion: reduce` is set
- WHEN the overlay opens or closes
- THEN all animations MUST be disabled
