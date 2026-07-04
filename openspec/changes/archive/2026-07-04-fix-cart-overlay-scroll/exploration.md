## Exploration: fix-cart-overlay-scroll

### Current State

#### Issue 1: Background scroll when overlay is open
- `abrirCarrito()` in `index.html` (line 630) only removes the `hidden` attribute from the overlay — it does NOT lock body scroll.
- `cerrarCarrito()` (line 641) only sets `hidden` back — no scroll restoration.
- No `document.body.style.overflow = 'hidden'` is set anywhere in the overlay lifecycle.
- The overlay uses `position: fixed; inset: 0; z-index: 1000` with a `.overlay-backdrop` (rgba black, 0.5 opacity), but since the body keeps scrolling, the backdrop scrolls with it. The overlay panel itself stays fixed, but the background page and backdrop shift on scroll.
- No scroll-lock mechanism exists anywhere in the project.

#### Issue 2: Mobile double-tap zoom on cart buttons
- The CSS property `touch-action` is NEVER used anywhere in the project (confirmed via grep).
- The viewport meta tag is `<meta name="viewport" content="width=device-width, initial-scale=1.0">` — no `user-scalable=no` (correct — we don't want to kill accessibility).
- Cart overlay interactive elements use the `.touch-target` class (min 44×44px for WCAG), but this does not prevent the ~300ms tap delay or the double-tap zoom gesture that iOS Safari triggers on `button`/`a` elements.
- Affected elements in the overlay: `.qty-btn`, `.overlay-close`, `.overlay-item-remove`, and the checkout/clear buttons.

#### Issue 3: Sticky bar background
- `.sticky-bar` in `main.css` (line 109) sets `background: var(--bg-primary, #fff)`.
- `--bg-primary` is **never defined** anywhere in the project (confirmed via grep — only appears at line 109). It falls back to `#fff` in standard CSS, but:
  - `#fff` (pure white) contrasts visually with the site's warm page background `--color-fondo: #F9F3EC`, creating a harsh seam.
  - The undefined custom property `--bg-primary` is a code smell — it should be `--color-fondo` or `--color-crema` from the design system.
  - On mobile at `max-width: 1024px`, there's a responsive override at line 499 that only adjusts padding/gap, not background.
- The hero section (gradient rosa background) scrolls behind the sticky bar. With a mismatched white background, content transparency may appear worse on certain screens/browsers.

#### Issue 4: Search bar position
- Current DOM order in `index.html`:
  1. **Line 514**: `.sticky-bar` (search form + cart icon)
  2. **Line 536**: `.hero` (banner with brand name)
  3. **Line 543**: `.filtros-bar` (category/subcategory selects)
  4. **Line 574**: `.productos-grid`
- The search bar appears at the very top of the page, above the hero/banner. This is the opposite of the expected layout — the hero should be first, then the search bar should appear where the filters start.
- Moving `.sticky-bar` below `.hero` changes the layout so the user sees the brand banner first, then the search bar sticks at the top as they scroll past it.

### Affected Areas
- `src/main/resources/templates/index.html` — Contains the overlay HTML, `abrirCarrito`/`cerrarCarrito` JS functions, `.sticky-bar` DOM position, and inline `<style>` for overlay and layout. Primary file for all 4 fixes.
- `src/main/resources/static/css/main.css` — Defines `.sticky-bar` background (undefined `--bg-primary` variable) and `.touch-target` utility. Needs background fix and potentially `touch-action: manipulation` rule.
- `src/main/resources/static/js/carrito-compartido.js` — Shared cart functions; not directly affected by these issues, but changes should be consistent with how this module works.

### Approaches

**Issue 1: Background scroll**
1. **Body scroll lock in JS** — Add `document.body.style.overflow = 'hidden'` in `abrirCarrito()` and restore with `document.body.style.overflow = ''` in `cerrarCarrito()`. Include a `scrollY` save/restore for iOS Safari (which ignores `overflow: hidden` on body in some contexts).
   - Pros: Simple, targeted, no new CSS needed.
   - Cons: Need to handle iOS Safari quirk (use `position: fixed` on body as well).
   - Effort: Low (5-10 lines of JS)

2. **CSS-based approach** — Add a `.no-scroll` class to body that sets `overflow: hidden; position: fixed; width: 100%;` and toggle it via JS. This handles iOS better.
   - Pros: Reliable cross-browser, reusable pattern.
   - Cons: Slightly more CSS + JS.
   - Effort: Low

**Issue 2: Double-tap zoom**
1. **Add `touch-action: manipulation` to overlay interactive elements** — Add the CSS property to `.overlay button`, `.overlay .qty-btn`, `.overlay-close`, and `.overlay-item-remove` selectors, or broadly to all overlay interactive elements.
   - Pros: Eliminates 300ms delay and double-tap zoom on those elements. WCAG-friendly (users can still pinch-zoom the page).
   - Cons: None significant.
   - Effort: Very low (2-3 CSS declarations)

2. **Add a utility class** — Create a `.touch-manipulation` class with `touch-action: manipulation` and apply it to all touch-target interactive elements across the site.
   - Pros: Reusable, benefits entire app.
   - Cons: More elements affected (review needed for each).
   - Effort: Low (1 CSS line + class additions)

**Issue 3: Sticky bar background**
1. **Use theme variable** — Change `var(--bg-primary, #fff)` to `var(--color-fondo)` or `var(--color-crema)` in `.sticky-bar` in `main.css`.
   - Pros: Uses existing design system variable, matches page background.
   - Cons: None.
   - Effort: Very low (1 line change)

2. **Define `--bg-primary` in `:root`** — Add `--bg-primary: var(--color-fondo);` to the `:root` in `main.css`.
   - Pros: Preserves the abstraction, other elements could use it.
   - Cons: Adds a variable for one consumer, minimal benefit.
   - Effort: Very low

**Issue 4: Search bar position**
1. **Move `.sticky-bar` DOM element below `.hero`** — In `index.html`, move the entire `<div class="sticky-bar">` block (lines 514-534) to between the hero (line 541) and the filtros-bar (line 543).
   - Pros: Corrects layout as requested, simple one-time reorder.
   - Cons: Changes page load rendering order (search form elements won't be above the fold initially).
   - Effort: Low (cut/paste + verify responsive)
   - Note: The `.sticky-bar` has `position: sticky; top: 0` so it will stick to the top once scrolled past the hero.

### Recommendation

| Issue | Approach | Rationale |
|-------|----------|-----------|
| 1 - Background scroll | **CSS class toggle** (approach 2) | More reliable cross-browser, especially for iOS Safari. Adding `.no-scroll` class to body with `position: fixed; overflow: hidden; width: 100%; top: -{scrollY}px` is the battle-tested pattern. |
| 2 - Double-tap zoom | **Add `touch-action: manipulation` to overlay elements** (approach 1) | Targeted fix for the reported problem. Can be broadened later. No user-scalable meta changes needed (preserves accessibility). |
| 3 - Sticky bar background | **Use `var(--color-fondo)`** (approach 1) | Simplest fix, uses the existing design token, visually consistent with the page. |
| 4 - Search bar position | **Move DOM element** (approach 1) | Direct fix for the reported issue. Only DOM reordering needed, no CSS changes for positioning. |

### Risks
- **iOS Safari scroll lock**: The `body { overflow: hidden }` approach alone does NOT work on iOS Safari (the body element isn't the scrolling container). The `position: fixed + top` approach is necessary. Must test on real iOS devices.
- **Search bar position change**: Moving the search bar below the hero may affect the infinite scroll sentinel and the search form submission behavior if there are any JS assumptions about search bar position relative to other elements. Verify no JS selects by `document.forms[0]` order or parent traversal.
- **Touch-action on overlay only**: If users interact with the body behind the overlay (which is the bug itself), the touch-action fix only covers overlay elements. Fix scroll lock first, then touch-action is a nice-to-have.
- **Sticky bar background**: After the sticky bar moves below the hero on mobile, the sticky bar's `top: 0` behavior combined with a long banner could mean the bar is not initially sticky at page load. This is the desired behavior per the user's report.

### Ready for Proposal
Yes
