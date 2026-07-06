# Propuesta: MejorarVisualCodigo — Reestructuración de Front-end

## Intención

Eliminar la duplicación masiva de CSS/JS inline en 8 plantillas Thymeleaf. 4 copias de PROMO, 518 líneas de CSS del carrito inline, hero duplicado en 6 templates — todo sin separación de responsabilidades. Modularizar preservando apariencia y comportamiento.

## Alcance

### Incluye
1. **Fase 1 — Deduplicación crítica**: PROMO → `static/js/promo.js` (4→1). CSS cart-overlay → `static/css/components/cart-overlay.css`. CSS+JS de toast → archivos componente. Eliminar `carrito.html` + endpoint del controlador.
2. **Fase 2 — CSS + Fragmentos**: Extraer `.hero`, `.breadcrumbs`, empty-state a CSS componente. Crear `dashboard.css`, `index.css`, `detalle.css`. Consolidar los 3 bloques `<style>` del dashboard en un archivo. Extraer `hero.html`, `breadcrumbs.html`, `empty-state.html` como fragmentos Thymeleaf. Incluir fragmento toast en `compras.html`, `movements.html`, `dashboard.html`.

### Excluye
JS modularización adicional (Fase 3 diferida), layout dialect / boilerplate HTML (Fase 4 diferida), cambios en variables CSS o sistema de diseño, reestructuración de backend.

## Capacidades

### Nuevas capacidades
Ninguna — reestructuración pura sin nuevos comportamientos de producto.

### Capacidades modificadas
Ninguna — `readability-baseline`, `responsive-layout`, `internal-architecture`, `product-listing` definen requisitos de comportamiento que no cambian al mover CSS/JS a archivos externos.

## Enfoque

2 fases secuenciales. Por cada extracción: (1) crear archivo destino, (2) reconciliar diferencias entre copias (PROMO requiere revisión manual de propiedades distintas en cada archivo), (3) reemplazar bloque inline con `<link>` o `<script src>`, (4) verificar apariencia idéntica página por página.

## Áreas Afectadas

| Área | Impacto |
|------|---------|
| `static/js/promo.js` | Nuevo |
| `static/css/components/cart-overlay.css` | Nuevo |
| `static/css/components/toast.css`, `static/js/toast.js` | Nuevos |
| `static/css/components/hero.css` | Nuevo |
| `static/css/components/breadcrumbs.css` | Nuevo |
| `static/css/components/empty-state.css` | Nuevo |
| `static/css/pages/{dashboard,index,detalle}.css` | Nuevos |
| `templates/carrito.html` | Eliminado |
| `templates/fragments/{hero,breadcrumbs,empty-state}.html` | Nuevos |
| `templates/*.html` (8 archivos) | Modificado |
| Controlador Java (endpoint `/productos/carrito`) | Modificado |

## Riesgos

| Riesgo | Prob. | Mitigación |
|--------|-------|------------|
| Especificidad CSS alterada (inline → external) | Media | Auditoría post-extracción página por página |
| Diferencias PROMO entre 4 copias | Media | Revisar cada copia, elegir la más completa |
| Endpoint carrito usado externamente | Baja | Verificar referencias antes de eliminar |
| Regresión visual | Media | Revisión página por página obligatoria |

## Plan de Rollback

`git revert` por fase. Si hay regresión en Fase 1, revertir antes de iniciar Fase 2. `carrito.html` se restaura desde git si es necesario.

## Dependencias

Ninguna externa. Se requiere revisión manual de diferencias PROMO antes de consolidar.

## Criterios de Éxito

- [ ] 8 plantillas con apariencia idéntica (comparación visual página por página)
- [ ] Sin errores en consola del navegador
- [ ] PROMO definido en 1 archivo (no 4)
- [ ] `carrito.html` + endpoint eliminados, app compila y arranca
- [ ] Dashboard, compras, movements incluyen toast funcional
- [ ] `mvnw.cmd compile` exitoso
