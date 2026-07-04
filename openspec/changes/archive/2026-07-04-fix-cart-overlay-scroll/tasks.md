# Tasks: Fix Cart Overlay Scroll

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~50–70 |
| 400-line budget risk | Low |
| Chained PRs recommended | No |
| Suggested split | Single PR |
| Delivery strategy | ask-on-risk |
| Chain strategy | size-exception |

```
Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: size-exception
400-line budget risk: Low
```

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | CSS foundation + HTML/JS integration | PR 1 (single) | All fixes; tests are manual verification only |

## Phase 1: CSS Foundation — `main.css`

- [x] **1.1** Fix `.sticky-bar` background: replace `var(--bg-primary, #fff)` with `var(--color-fondo)` in `main.css:109`
- [x] **1.2** Add `.no-scroll` class block: `overflow: hidden; position: fixed; width: 100%` — placed after `.sticky-bar` section (~line 128)
- [x] **1.3** Add `touch-action: manipulation` group selector: `button, a, input, select, textarea, .qty-btn, .overlay-close, .overlay-item-remove, .btn-whatsapp-comprar` — after the `.no-scroll` block

## Phase 2: HTML/JS Integration — `index.html`

- [x] **2.1** Move `.sticky-bar` DOM block (lines 514–534) after `.hero` closing tag (line 541) — sticky bar sits between `.hero` and `.filtros-bar`
- [x] **2.2** In `abrirCarrito()`: save `window.scrollY`, add `.no-scroll` to `document.body`, set `body.style.top = -scrollY + 'px'`
- [x] **2.3** In `cerrarCarrito()`: remove `.no-scroll` from `document.body`, reset `body.style.top`, call `window.scrollTo(0, savedScrollY)`
