## Exploration: Unificación visual de producto-card

### Current State

El card de producto (`.producto-card`) se renderiza actualmente como 3 bloques visualmente separados dentro de un contenedor `<a>`:

1. **Imagen** (`.card-img`) — 200px de alto, centrada, sin padding ni borde propio. Se apoya en `overflow: hidden` del padre para respetar el borde redondeado superior.
2. **Detalles** (`.card-body`) — nombre, marca, precio, stock. Padding interno `0`, los hijos usan `margin: 0 14px` (excepto el botón). Sin separación visual de la imagen — comparten el borde exterior.
3. **Botón "Comprar"** (`.btn-comprar`) — Dentro de `.card-body` pero con `margin-left: 0; margin-right: 0` para romper el padding lateral. **Tiene su propio `border-radius: 10px`** y `background: #25D366`, visualmente un tercer bloque con esquinas redondeadas propias, casi pegado al bloque de detalles.

**Archivos afectados:**

| Archivo | Rol |
|---------|-----|
| `src/main/resources/templates/fragments/producto-cards.html` | Template Thymeleaf del fragmento `cardGrid` |
| `src/main/resources/templates/index.html` | Página principal — contiene el `<style>` con todo el CSS del card (líneas 86-164) |
| `src/main/resources/static/css/main.css` | CSS global — actualmente solo define variables, reset, utilidades. No contiene estilos de card |
| `src/main/resources/templates/detalle.html` | Otra variante de card (`.rel-card`) para productos relacionados |

**HTML actual del fragmento:**
```html
<a class="producto-card" th:each="p : ${productos}"
   th:classappend="${p.stock == 0} ? 'sin-stock'"
   th:href="@{/productos/detalle/{n}/{s}(n=${p.name}, s=${p.subCategory})}"
   role="article"
   th:aria-label="${p.name + ' - $' + #numbers.formatDecimal(p.price, 1, 2)}">
  <div class="card-img">...</div>
  <div class="card-body">
    <div class="card-title" th:text="${p.name}">Nombre</div>
    <span class="card-brand" ...>AVON</span>
    <div class="card-price" th:text="'$' + ...">$0.00</div>
    <div class="card-stock-status">...</div>
    <a class="btn-comprar touch-target focus-visible"
       th:href="@{https://wa.me/...}" target="_blank"
       onclick="event.stopPropagation()">💬 Comprar</a>
  </div>
</a>
```

**CSS actual relevante (extractos de index.html `<style>`):**
```css
.producto-card {
    background: #fff;
    border: 1.5px solid var(--color-rosa-borde);
    border-radius: 14px;
    overflow: hidden;
    display: block;
}
.card-body { padding: 0; }
.card-body > * { margin: 0 14px; }
.card-body > :first-child { margin-top: 10px; }
.card-body > .btn-comprar { margin-left: 0; margin-right: 0; }
.btn-comprar {
    display: block; width: 100%;
    padding: 10px 14px;
    background: #25D366; color: white;
    border-radius: 10px;        /* ← propio border-radius, difiere del padre 14px */
    min-height: var(--touch-min);
}
```

**Problemas identificados:**

- `.btn-comprar` tiene `border-radius: 10px` ≠ `14px` del padre — inconsistencia visual
- El botón está pegado al contenido de detalles (sin `margin-top`)
- `.card-body` con `padding: 0` fuerza a usar `margin` en los hijos para el spacing interno, lo que complica el modelo de caja
- No hay separación visual clara entre secciones porque comparten el borde exterior, pero el botón "rompe" la unidad con su propio fondo y border-radius

### Affected Areas

- `src/main/resources/templates/fragments/producto-cards.html` — Template del card, podría requerir reestructuración menor del HTML
- `src/main/resources/templates/index.html` — CSS embebido del card (líneas 86-164) — TODOS los estilos del card están acá
- `src/main/resources/static/css/main.css` — Potencial destino si se decide mover estilos de card al CSS global
- `src/main/resources/templates/detalle.html` — El card relacionado (`.rel-card`) usa otro patrón, fuera del scope pero podría servir de referencia

### Approaches

1. **CSS-only fix (mínimo cambio)** — Mantener el HTML exacto, solo cambiar CSS
   - Sacar `border-radius` de `.btn-comprar` (hereda del padre `overflow: hidden`)
   - Agregar `margin-top` al botón para separarlo del stock
   - Cambiar `.card-body` a usar `padding` en vez de `margin` en hijos para simplificar
   - Pros: Sin cambios en HTML, bajo riesgo, fácil de revertir
   - Cons: El HTML sigue teniendo el botón anidado en `.card-body` con hacks (`margin-left: 0`)
   - Effort: Low

2. **Restructure HTML + CSS** — Mover `.btn-comprar` fuera de `.card-body`, hermano directo de `.card-img`
   - HTML: sacar el `<a.btn-comprar>` de `.card-body`, ponerlo como hijo directo del `<a.producto-card>`
   - CSS: `.card-body` usa `padding` consistente, `.btn-comprar` margin-top y sin border-radius propio
   - Pros: Estructura semántica más limpia, sin hacks de margin, más fácil de mantener
   - Cons: Cambia el HTML del fragmento (afecta solo a este fragmento)
   - Effort: Low

3. **Unificación total con main.css** — Mover todos los estilos de card a `main.css` como parte de la refactorización
   - Incluye el Approach 1 o 2 más mover CSS del `<style>` de index.html a `main.css`
   - Pros: CSS compartido reusable, un solo lugar para modificar, los cards de detalle también podrían beneficiarse
   - Cons: Cambio más grande, tocar main.css y verificar que no rompa otras páginas
   - Effort: Medium

### Recommendation

**Approach 2 (Restructure HTML + CSS)** — es el que mejor resuelve el problema de raíz:

1. Mover `.btn-comprar` fuera de `.card-body` como hijo directo de `.producto-card`, después de `.card-body`.
2. Eliminar `border-radius` del botón — el padre con `overflow: hidden` se encarga de las esquinas.
3. Cambiar `.card-body` a usar `padding: 10px 14px 0` en vez de `padding: 0` con margins en hijos.
4. Agregar `margin-top: 10px` al botón (o `gap` en el padre si se convierte en flex column).
5. Quitar reglas hack (`card-body > .btn-comprar { margin-left: 0; margin-right: 0; }`).

Esto da un solo contenedor con un border-radius unificado, el botón integrado visualmente pero con separación del contenido, y padding consistente en todo el card.

Esfuerzo bajo, impacto limitado al fragmento y su CSS.

### Risks

- El botón actualmente captura clicks con `event.stopPropagation()` — al moverlo de lugar en el DOM hay que verificar que siga funcionando (sigue siendo un `<a>` dentro de otro `<a>`, el `stopPropagation` previene que el click en comprar navegue al detalle).
- La altura fija de `.card-img` (200px) podría necesitar ajuste si se cambia el modelo de caja.
- Si se decide hacer Approach 3 (mover a main.css), verificar que el CSS no entre en conflicto con otras páginas (detalle.html tiene su propio `.rel-card`).
- Verificar que el responsive (2 columnas en mobile, gap 10px) siga funcionando bien después de cambios de padding/margin.

### Ready for Proposal

Yes. La exploración está completa y el enfoque es claro. El orchestrator puede decirle al usuario: "El card actual tiene el botón con su propio border-radius dentro del contenedor. La solución es mover el botón fuera del card-body como hermano directo, eliminar su border-radius, y usar padding consistente. Es un cambio de esfuerzo bajo en 2 archivos."
