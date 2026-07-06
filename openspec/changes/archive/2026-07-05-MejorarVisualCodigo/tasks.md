# Tasks: MejorarVisualCodigo

## Review Workload Forecast

| Campo | Valor |
|-------|-------|
| Líneas modificadas estimadas | ~2500 |
| Riesgo presupuesto 400 líneas | Alto |
| ¿PR encadenados recomendados? | Sí |
| División sugerida | PR 1 → PR 2 |
| Estrategia de entrega | ask-always |
| Estrategia de cadena | pendiente |

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: pending
400-line budget risk: High

### Suggested Work Units

| Unidad | Objetivo | PR propuesto |
|--------|----------|--------------|
| 1 | Deduplicación crítica (PROMO, overlay, toast, carrito muerto) | PR 1 → main |
| 2 | CSS + Fragmentos (componentes, páginas, fragments, toast faltante) | PR 2 → main |

## Fase 1: Deduplicación Crítica

- [x] 1.1 Crear `static/js/promo.js` — PROMO, promoActiva, actualizarCountdown desde 4 copias
- [x] 1.2 Modificar `carrito-compartido.js` — remover PROMO + promoActiva
- [x] 1.3 Modificar `detalle.html` — PROMO inline → `<script src="/js/promo.js">`
- [x] 1.4 Crear `static/css/components/cart-overlay.css` — extraer 518 líneas overlay
- [x] 1.5 Crear `static/css/components/toast.css` + `static/js/toast.js`
- [x] 1.6 Modificar `toast.html` — HTML puro, enlazar CSS + JS externos
- [x] 1.7 Modificar `index.html` — overlay CSS → `<link>`, toast via externos, agregar `<script src="/js/promo.js">` antes de carrito-compartido.js
- [x] 1.8 Eliminar `carrito.html` + endpoint `/productos/carrito` en `ProductController.java`
- [x] 1.9 Verificar: `mvnw.cmd compile`, revisar index + detalle side-by-side, consola 0 errores; verificar orden de scripts en DevTools (promo.js antes de carrito-compartido.js)

## Fase 2: CSS + Fragmentos

- [x] 2.1 Crear `components/hero.css`, `components/breadcrumbs.css`, `components/empty-state.css`
- [x] 2.2 Crear `pages/dashboard.css` — consolidar 3 bloques `<style>` + modal CSS
- [x] 2.3 Crear `pages/index.css` — grid, filtros, paginación
- [x] 2.4 Crear `pages/detalle.css` — detalle-card, relacionados, gift-banner
- [x] 2.5 Crear fragmentos: `hero.html`, `breadcrumbs.html`, `empty-state.html`
- [x] 2.6 Modificar `compras.html`, `movements.html` — agregar fragmento toast + extraer CSS → `pages/compras.css`, `pages/movements.css`
- [x] 2.7 Crear `static/js/dashboard.js` — función initDashboardCharts (carga DESPUÉS de Chart.js)
- [x] 2.8 Modificar `dashboard.html` — CSS consolidado → `pages/dashboard.css`, datos inline, initDashboardCharts externo, incluye toast
- [x] 2.9 Modificar `form.html` → CSS a `pages/form.css`, `admin-login.html` → CSS a `pages/admin-login.css`
- [x] 2.10 Verificar: `mvnw.cmd compile` exitoso, 0 errores

## Fase 3: JS Cleanup

- [x] 3.1 Crear `static/js/detalle.js` — extraer actualizarPromoBanner, cambiarQty, agregarAlCarrito, initDetalle. ELIMINAR getCarrito/guardarCarrito duplicados (ya existen en carrito-compartido.js)
- [x] 3.2 Crear `static/js/dashboard-stock.js` — extraer ajustarStockDash, confirmarEliminacion
- [x] 3.3 Crear `static/js/dashboard-modal.js` — extraer openModal, closeModal, filtrarTabla
- [x] 3.4 Modificar `detalle.html` — reemplazar inline JS con `<script src="/js/detalle.js">` + `<script src="/js/carrito-compartido.js">`, eliminar getCarrito/guardarCarrito inline
- [x] 3.5 Modificar `dashboard.html` — reemplazar inline JS de stock con `<script src="/js/dashboard-stock.js">`, reemplazar inline JS de modal con `<script src="/js/dashboard-modal.js">`
- [x] 3.6 Verificar: `mvnw.cmd compile` (BUILD SUCCESS), revisar dashboard + detalle side-by-side, consola 0 errores
