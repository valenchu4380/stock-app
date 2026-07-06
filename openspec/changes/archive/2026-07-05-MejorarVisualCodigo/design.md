# Diseño Técnico: MejorarVisualCodigo — Reestructuración Front-end

## Enfoque Técnico

Migración segura de CSS/JS inline en 8 plantillas Thymeleaf a archivos externos, preservando apariencia visual. Dos fases secuenciales: (1) deduplicación crítica (PROMO, cart-overlay, toast, eliminar carrito), (2) modularización CSS + fragmentos. Cada extracción sigue: crear archivo → reconciliar diferencias → reemplazar bloque inline → verificar visualmente.

## Decisiones Arquitectónicas

### Organización de archivos

```
static/css/
├── main.css                    (shared — reset, custom props, utilidades)
├── components/
│   ├── cart-overlay.css        (518 líneas — overlay completo del carrito)
│   ├── toast.css               (CSS del toast/dialog — extraído de toast.html)
│   ├── hero.css                (hero gradient + tipografía)
│   ├── breadcrumbs.css         (navegación migas de pan)
│   └── empty-state.css         (estado vacío con icono + acción)
└── pages/
    ├── index.css               (product-grid, filtros, paginación)
    ├── detalle.css              (detalle-card, relacionados, gift-banner)
    ├── dashboard.css            (stats, charts, tabla gestión, modal)
    ├── form.css                 (formulario producto — hero, form-groups)
    ├── compras.css              (tabla órdenes, badges, responsive cards)
    ├── movements.css            (tabla movimientos, badges, paginación)
    └── admin-login.css          (login card centrado, form controls)

static/js/
├── carrito-compartido.js       (shared — KEEP, remove PROMO)
├── promo.js                    (CREAR — PROMO + promoActiva + actualizarCountdown)
├── toast.js                    (CREAR — showToast, showConfirm, showPrompt, trapFocus)
└── dashboard.js                (CREAR — initDashboardCharts)
```

### Especificidad CSS (inline → external)

| Aspecto | Riesgo | Mitigación |
|---------|--------|------------|
| `<style>` en `<head>` vs `<link>` | Misma especificidad (origen autor) | Orden de carga: `main.css` → `components/*.css` → `pages/*.css`. El último gana. |
| Estilos inline (`style="..."`) | Especificidad 1,0,0,0 — no se modifican | NO tocar atributos `style=""`. Solo extraer bloques `<style>`. |
| `<style>` en `<body>` | Misma especificidad pero orden source diferente | Migrar TODO `<style>` a archivos externos en `<head>` para orden predecible. |

**Regla de oro**: La apariencia visual es IDÉNTICA porque `<style>` y `<link>` tienen la misma especificidad. El único riesgo es si queda un `<style>` residual en `<body>` que sobrescriba un `<link>` en `<head>` — por eso se extraen TODOS los bloques.

### Dashboard Chart.js — th:inline="javascript"

No se puede externalizar completamente porque Thymeleaf procesa `/*[[${labels}]]*/` en servidor. Estrategia: mantener la declaración de datos inline, mover la lógica de inicialización a `dashboard.js`.

```html
<!-- INLINE (se queda) — solo datos -->
<script th:inline="javascript">
    const labels = /*[[${labels}]]*/ [];
    const ganancias = /*[[${ganancias}]]*/ [];
    // ... resto de datos ...
    document.addEventListener('DOMContentLoaded', () => initDashboardCharts({
        labels, ganancias, costos, ventas, margenes, catLabels, catGanancias, lineaLabels, lineaGanancias
    }));
</script>
<!-- EXTERNAL — lógica de Chart.js -->
<script src="/js/dashboard.js"></script>
```

### PROMO — reconciliación de 4 copias

| Copia | Archivo | Declaración | `actualizarCountdown` | Función extra |
|-------|---------|-------------|----------------------|---------------|
| #1 | `carrito-compartido.js` | `var PROMO` | ❌ | `enviarPedido` lo referencia |
| #2 | `index.html` | (usa la de #1) | ❌ | `actualizarPromoEnOverlay` |
| #3 | `detalle.html` | `const PROMO` (duplicada) | ✅ versión simple | `actualizarPromoBanner` |
| #4 | `carrito.html` | `const PROMO` (duplicada) | ✅ con check `promoActiva()` | `renderizarCarrito` |

**Estrategia**: Valores de PROMO son IDÉNTICOS en las 4 copias. Crear `promo.js` con `var PROMO` + `promoActiva()` + `actualizarCountdown()` (versión de carrito.html — más completa, con chequeo de `promoActiva`). Las funciones específicas de página (`actualizarPromoEnOverlay`, `actualizarPromoBanner`, `renderizarCarrito`) se quedan en sus templates.

### Orden de carga de CSS

```
1. main.css                     (reset, custom properties, utilidades base)
2. components/toast.css         (toast — necesario desde el primer paint)
3. components/cart-overlay.css  (overlay — solo index.html)
4. components/hero.css          (hero — si template lo necesita)
5. components/breadcrumbs.css   (breadcrumbs — si template lo necesita)
6. components/empty-state.css   (empty state — si template lo necesita)
7. pages/{pagina}.css           (específico de página — carga última para sobrescribir)
```

## Orden de Extracción

### Fase 1 (más riesgosa — PROMO + carrito-dead-code primero)

1. **Crear `promo.js`** — extraer PROMO + promoActiva + actualizarCountdown de carrito-compartido.js y las copias inline
2. **Modificar `carrito-compartido.js`** — eliminar PROMO + promoActiva
3. **Modificar `detalle.html`** — reemplazar PROMO inline con `<script src="/js/promo.js">`, mantener `actualizarPromoBanner`
4. **Crear `cart-overlay.css`** — extraer 518 líneas de overlay de index.html
5. **Crear `toast.css` + `toast.js`** — extraer de toast.html
6. **Modificar `toast.html`** — dejar solo HTML puro, con `<link>` y `<script>` apuntando a externos
7. **Modificar `index.html`** — reemplazar CSS overlay con `<link>`, agregar `<script src="/js/promo.js">` ANTES de `carrito-compartido.js`, toast incluye externos
8. **Eliminar `carrito.html` + endpoint** — verificar que no hay referencias
9. **VERIFICAR**: compilar, revisar página por página

### Fase 2 (CSS + fragmentos)

10. **Crear `hero.css`** — extraer de index (usar ese como base, verificar dashboard/form/movements/detalle tienen mismas propiedades)
11. **Crear `breadcrumbs.css`** — unificar diferencias entre form, dashboard, compras, movements
12. **Crear `empty-state.css`** — extraer de index (usar como base)
13. **Crear `dashboard.css`** — consolidar los 3 bloques `<style>` + modal CSS
14. **Crear `index.css`** — lo que queda de index.html después de overlay y hero
15. **Crear `detalle.css`** — extraer de detalle.html
16. **Crear `compras.css`** — extraer CSS de compras.html
17. **Crear `movements.css`** — extraer CSS de movements.html
18. **Crear `form.css`** — extraer CSS de form.html
19. **Crear `admin-login.css`** — extraer CSS de admin-login.html
20. **Crear fragmentos** — hero.html, breadcrumbs.html, empty-state.html
21. **Crear `dashboard.js`** — initDashboardCharts (MUST ser función global `window.initDashboardCharts`, cargar DESPUÉS de Chart.js)
22. **Modificar `dashboard.html`** — link a `dashboard.css`, datos inline, initDashboardCharts externo, incluye toast
23. **Modificar `index.html`** — link a `index.css`, `hero.css`, `breadcrumbs.css`, `empty-state.css`
24. **Modificar `detalle.html`** — link a `detalle.css`, `hero.css`, `breadcrumbs.css`
25. **Modificar `form.html`** — link a `form.css`
26. **Modificar `compras.html`** — link a `compras.css`, agregar fragmento toast
27. **Modificar `movements.html`** — link a `movements.css`, agregar fragmento toast
28. **Modificar `admin-login.html`** — link a `admin-login.css`
29. **VERIFICAR**: compilar, revisar 8 páginas side-by-side, consola 0 errores, verificar orden de carga scripts

## Cambios de Archivos

| Archivo | Acción | Descripción |
|---------|--------|-------------|
| `static/js/promo.js` | Crear | PROMO + promoActiva + actualizarCountdown |
| `static/css/components/cart-overlay.css` | Crear | 518 líneas de overlay desde index.html |
| `static/css/components/toast.css` | Crear | Toast/dialog CSS desde toast.html |
| `static/js/toast.js` | Crear | showToast, showConfirm, showPrompt, trapFocus |
| `static/css/components/hero.css` | Crear | Hero gradient + tipografía |
| `static/css/components/breadcrumbs.css` | Crear | Breadcrumbs unificado |
| `static/css/components/empty-state.css` | Crear | Empty state unificado |
| `static/css/pages/index.css` | Crear | Index-specific (grid, filtros, paginación) |
| `static/css/pages/detalle.css` | Crear | Detalle-specific |
| `static/css/pages/dashboard.css` | Crear | Dashboard (3 bloques consolidados + modal) |
| `static/css/pages/compras.css` | Crear | Compras-specific (tabla, badges, responsive cards) |
| `static/css/pages/movements.css` | Crear | Movements-specific (tabla, badges, paginación) |
| `static/css/pages/form.css` | Crear | Form-specific (hero, form-groups, breadcrumbs) |
| `static/css/pages/admin-login.css` | Crear | Admin-login-specific (login card centrado) |
| `static/js/dashboard.js` | Crear | Funciones initDashboardCharts |
| `templates/fragments/hero.html` | Crear | Fragmento hero con parámetros |
| `templates/fragments/breadcrumbs.html` | Crear | Fragmento breadcrumbs |
| `templates/fragments/empty-state.html` | Crear | Fragmento empty state |
| `static/js/carrito-compartido.js` | Modificar | Remover PROMO + promoActiva |
| `templates/fragments/toast.html` | Modificar | Remover CSS+JS, dejar HTML puro |
| `templates/index.html` | Modificar | Extraer CSS overlay, hero, page CSS; agregar links |
| `templates/detalle.html` | Modificar | Extraer CSS, PROMO inline, agregar links |
| `templates/dashboard.html` | Modificar | Consolidar 3 CSS → 1 archivo, agregar toast, init data inline |
| `templates/form.html` | Modificar | Extraer CSS hero + breadcrumbs + form |
| `templates/compras.html` | Modificar | Extraer CSS, agregar toast fragment |
| `templates/movements.html` | Modificar | Extraer CSS, agregar toast fragment |
| `templates/admin-login.html` | Modificar | Extraer CSS login |
| `ProductController.java` | Modificar | Eliminar endpoint `/carrito` (líneas 498-502) |
| `templates/carrito.html` | Eliminar | Dead code — carrito ahora es overlay |
| `static/css/components/product-card.css` | NO crear | Cards están en `index.css` + fragmento |

## Estrategia de Verificación

| Capa | Qué verificar | Cómo |
|------|--------------|------|
| Compilación | `mvnw.cmd compile` exitoso | Ejecutar después de cada fase |
| Visual | Apariencia página vs original | Comparar side-by-side en navegador — mismas dimensiones, colores, espaciado |
| Consola | 0 errores JS, 0 404 de recursos | Abrir DevTools en cada página post-migración |
| Funcional | Cart overlay, toast, Chart.js, PROMO | Click-test: abrir carrito, agregar/quitar items, verificar contador, check countdown |
| Chart.js | Gráficos se renderizan | Verificar que canvas muestren datos, no errores |
| Toast Faltantes | compras, movements, dashboard | `showToast('test','success')` en consola → debe mostrar toast |

### Protocolo de verificación entre fases

```
Fase 1 completo → git commit + tag "phase-1"
  → Verificar: index, detalle sin PROMO duplicado, overlay funcional
  → Compilar y revisar consola
Fase 2 completo → git commit + tag "phase-2"
  → Verificar TODAS las 8 páginas side-by-side
  → Sin errores, sin cambios visuales
```

## Migración / Rollout

Por fase, con commits independientes. Rollback simple con `git revert <commit>`. No requiere feature flags. `carrito.html` se restaura desde git si es necesario.

## Riesgos

| Riesgo | Prob. | Mitigación |
|--------|-------|------------|
| CSS `<style>` residual en `<body>` pisa external CSS | Baja | Extraer TODOS los bloques. Audit post-fase-2. |
| PROMO undefined si orden de scripts incorrecto | Media | `promo.js` debe cargarse ANTES que `carrito-compartido.js` en templates que usan ambos. |
| `th:inline="javascript"` roto al mover | Baja | NO externalizar — mantener inline, solo datos. Lógica va a dashboard.js. |
| `form.html` tiene `subcategorias` duplicado de `index.html` | Baja | Fase 3 (fuera de alcance). Dejar como está. |
| Endpoint carrito usado por bookmark externo | Baja | Verificar logs de acceso o ignorar — 404 es manejable. |
