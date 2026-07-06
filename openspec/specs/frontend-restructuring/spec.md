# Delta para Reestructuración Front-end

## Propósito

Eliminar CSS/JS inline en 8 plantillas Thymeleaf extrayendo estilos y scripts a archivos externos. Reestructuración pura: comportamiento visual y funcional idéntico.

## Contexto

Los specs `internal-architecture`, `readability-baseline`, `responsive-layout` y `product-listing` definen comportamientos que NO cambian. Solo cambia la ubicación del CSS/JS de bloques inline a archivos externos vinculados con `<link>` y `<script src>`.

---

## ADDED Requirements

### R-FS01: Organización de archivos estáticos

Los CSS extraídos MUST organizarse en `static/css/components/` (reutilizables) y `static/css/pages/` (página específica). JS extraídos MUST ir a `static/js/`. Fragmentos Thymeleaf MUST ir a `templates/fragments/`.

| Archivo | Origen | Tipo |
|---------|--------|------|
| `static/css/components/cart-overlay.css` | Inline en `index.html` | Componente |
| `static/css/components/toast.css` | Inline en `toast.html` | Componente |
| `static/css/components/hero.css` | Inline duplicado en 6 templates | Componente |
| `static/css/components/breadcrumbs.css` | Inline duplicado | Componente |
| `static/css/components/empty-state.css` | Inline duplicado | Componente |
| `static/css/pages/dashboard.css` | 3 bloques inline en `dashboard.html` | Página |
| `static/css/pages/index.css` | Inline en `index.html` | Página |
| `static/css/pages/detalle.css` | Inline en `detalle.html` | Página |
| `static/css/pages/compras.css` | Inline en `compras.html` | Página |
| `static/css/pages/movements.css` | Inline en `movements.html` | Página |
| `static/css/pages/form.css` | Inline en `form.html` | Página |
| `static/css/pages/admin-login.css` | Inline en `admin-login.html` | Página |
| `static/js/promo.js` | 4 copias PROMO inline | Componente |
| `static/js/toast.js` | Script inline en `toast.html` | Componente |
| `static/js/dashboard.js` | initDashboardCharts (nuevo) | Componente |
| `templates/fragments/hero.html` | Bloque duplicado en 6 templates | Fragmento |
| `templates/fragments/breadcrumbs.html` | Bloque duplicado | Fragmento |
| `templates/fragments/empty-state.html` | Bloque duplicado | Fragmento |

#### Scenario: Archivos creados en ruta correcta

- GIVEN el proyecto antes de la extracción
- WHEN se ejecuta la migración
- THEN cada archivo MUST crearse dentro de `src/main/resources/` en su ruta exacta
- AND MUST ser accesible desde el servidor estático de Spring Boot

#### Scenario: Sin colisiones

- GIVEN los archivos destino
- WHEN se listan en sus directorios
- THEN NO MUST sobrescribir archivos existentes no relacionados con este cambio

### R-FS02: Preservación visual y funcional

La apariencia visual y el comportamiento de cada template MUST ser idénticos post-extracción.

#### Scenario: Sin regresión visual

- GIVEN cualquier template antes de la extracción
- WHEN se compara con el mismo template después de la extracción
- THEN cada elemento MUST tener idéntico `computed style` (color, font-size, margin, padding, display)
- AND el layout MUST ser visualmente indistinguible

#### Scenario: Sin errores en consola

- GIVEN cualquier página post-extracción
- WHEN se carga en el navegador
- THEN la consola MUST mostrar 0 errores
- AND MUST mostrar 0 warnings de recursos no encontrados (404)

### R-FS03: Eliminación de carrito.html

`carrito.html` y su endpoint `/productos/carrito` en el controlador MUST eliminarse.

#### Scenario: Template y endpoint eliminados

- GIVEN el proyecto post-extracción
- WHEN se ejecuta `mvnw.cmd compile`
- THEN `src/main/resources/templates/carrito.html` NO MUST existir
- AND 0 referencias al endpoint `/productos/carrito` MUST existir en controladores Java

#### Scenario: Sin referencias rotas

- GIVEN que `carrito.html` fue eliminado
- WHEN se navega por el sitio
- THEN ningún enlace MUST apuntar a `/productos/carrito`
- AND la aplicación compila y arranca sin errores

### R-FS04: Integración de toast en templates faltantes

`compras.html`, `movements.html` y `dashboard.html` MUST incluir el fragmento toast completo (CSS + JS + HTML).

#### Scenario: Toast funcional

- GIVEN `compras.html`, `movements.html` o `dashboard.html` post-migración
- WHEN se dispara `showToast()`
- THEN el toast MUST mostrarse con estilo, animación y comportamiento idénticos a otras páginas

---

## MODIFIED Requirements

### Cart Overlay Instead of Navigation (spec: `product-listing`)
*(Implementación: CSS/JS inline → `cart-overlay.css` externo. Comportamiento: sin cambios)*

Todos los escenarios del overlay (apertura/cierre, body-scroll lock, iOS workaround, CRUD localStorage, focus trap, reduced motion) MUST seguir funcionando idénticamente.

### Add to Cart from Product Cards (spec: `product-listing`)
*(Implementación: toast inline → `toast.css` + `toast.js` externos. Comportamiento: sin cambios)*

`showToast()` MUST estar disponible globalmente. Animación, color, duración y contenido del toast MUST ser idénticos.

### Body font size ≥ 16px / WCAG AA contrast (spec: `readability-baseline`)
*(Implementación: CSS inline en templates → archivos página/componente. Valores: sin cambios)*

Los `computed styles` finales MUST ser idénticos. La ubicación del CSS no altera ninguna propiedad visual.

### Tablet breakpoint at 1024px / Tables collapse to cards (spec: `responsive-layout`)
*(Implementación: CSS inline → archivos página/componente. Comportamiento: sin cambios)*

Breakpoints, transiciones y collapse a cards MUST seguir funcionando sin cambios visibles.

---

## Verification Criteria

- `mvnw.cmd compile` exitoso
- Comparación visual página por página en las 8 plantillas (template original vs. refactorizado)
- Sin errores ni warnings 404 en consola del navegador
- Overlay de carrito funcional en `index.html` (sin redirección)
- Toast funcional en `compras.html`, `movements.html`, `dashboard.html`
- Dashboard con gráficos Chart.js renderizados correctamente (sin errores en canvas)
- Script loading order verificado: `promo.js` ANTES que `carrito-compartido.js`, `dashboard.js` DESPUÉS de Chart.js CDN
