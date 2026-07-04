# Visual Unification — Product Card

## Purpose

Unify `.producto-card` into a single visual block with consistent border-radius, padding-based spacing, and preserved event behavior after HTML restructure.

## Requirements

### Requirement: Uniform border-radius

The product card SHALL render as a single container with a uniform `14px` border-radius applied exclusively to `.producto-card`. The button MUST NOT have its own border-radius — its corners SHALL be clipped by the parent's `overflow: hidden`.

#### Scenario: Card renders as single block with consistent corners

- GIVEN a `.producto-card` with `.card-body` and `.btn-comprar` as direct children
- WHEN the card is rendered
- THEN `.producto-card` SHALL have `border-radius: 14px` and `overflow: hidden`
- AND `.btn-comprar` SHALL NOT apply its own `border-radius`

#### Scenario: Button bottom corners match container corners

- GIVEN a `.producto-card` containing `.btn-comprar` as the last child
- WHEN the card is displayed
- THEN the button's bottom-left and bottom-right corners SHALL follow the container's `14px` radius via `overflow: hidden`

### Requirement: Spacing via padding, not margin hacks

Spacing inside `.card-body` MUST use `padding` on the container. Margin-based positioning hacks on children MUST be removed.

#### Scenario: Card-body spacing uses padding

- GIVEN a `.producto-card` with `.card-body`
- WHEN rendered
- THEN `.card-body` SHALL have `padding: 10px 14px 0`
- AND no child selector inside `.card-body` SHALL use `margin` for layout positioning

### Requirement: Event propagation preserved after DOM restructure

Moving `.btn-comprar` to a sibling of `.card-body` MUST NOT break `event.stopPropagation()` on the WhatsApp button.

#### Scenario: WhatsApp icon click does not navigate card

- GIVEN a `.producto-card` with a WhatsApp icon inside `.btn-comprar`
- WHEN the WhatsApp icon is clicked
- THEN `event.stopPropagation()` SHALL prevent the card's parent anchor navigation
- AND the WhatsApp link SHALL open normally

### Requirement: Long product names handled without overflow

The card SHALL contain long product names without horizontal overflow or layout breakage within the 2-column mobile layout.

#### Scenario: Long product name stays inside card bounds

- GIVEN a `.producto-card` with a product name of 60+ characters
- WHEN rendered at 360px viewport width
- THEN the product name SHALL NOT overflow its container horizontally
- AND the card SHALL remain within its column bounds
