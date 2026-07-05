# Design: Optimización de Recursos

## Technical Approach

Replace Java in-memory aggregation (6 metrics loaded from `SELECT *` + loop) with PostgreSQL SQL aggregation (SUM/AVG/COUNT). Remove entire Linea enum/servicing system keeping only the DB column as `String linea` in Product. Delete dead code. Extract shared filter query builder to eliminate 5 redundant WHERE-clause copies.

## Architecture Decisions

### Decision: Product.linea type change

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Keep `Linea` enum, remove only service | Adds ~50 enum values with no logic, but compilation-safe initially | ❌ |
| Change to `String linea` | Loses display-name mapping, but DB stores enum name already — no mapping needed | ✅ |
| Delete field entirely | Would lose data on save/update touching the row | ❌ DB column preserved |

**Rationale**: The DB column `linea VARCHAR(255)` stores the enum `.name()`. Changing to `String` preserves data without the enum. `mapResult()` updates from `Linea.valueOf(linea)` to `linea` string directly. Chart labels show raw enum names (minor cosmetic change — acceptable).

### Decision: Dashboard SQL strategy

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Single multi-aggregate query | Returns all 6 metrics + chart data in 1 roundtrip | ✅ |
| One query per metric | Simple but 6+ roundtrips | ❌ |
| Keep Java loop + pagination | Reduces load but still joins in memory | ❌ defeats purpose |

**Rationale**: One query for aggregate metrics (`totalInversion`, `totalVenta`, `totalGanancia`, `countConCosto`, `margenPromedio`). One query for top-20 chart data. One query per chart grouping (category, subcategory, linea). ~4 queries replacing 1 Java loop over all rows.

### Decision: Filter query builder

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Extract `buildFilterQuery(select, params)` | Shared WHERE clause, eliminates 5× repetition | ✅ |
| Keep 5 copies | Duplicated code, risky to modify | ❌ |
| Use StringTemplate or QueryDSL | Adds dependency, overkill for 5 methods | ❌ |

**Rationale**: Each of the 5 methods (`countFiltered`, `sumInventario`, `sumStock`, `countSinStock`, `countStockBajo`) builds identical WHERE clauses. A private helper `buildFilterQuery(String select, List<Object> params, ...)` returns the PreparedStatement. `countSinStock` prepends `WHERE stock = 0` and `countStockBajo` prepends `WHERE stock BETWEEN 0 AND 1` before the shared filter block.

### Decision: Delete entire Linea system in one pass

| Component | Delete action | Cascade |
|-----------|-------------|---------|
| `Validates.java` | Delete file | No deps (confirmed dead) |
| `Linea.java` | Delete file | Change `Product.linea` to `String`, update all callers |
| `LineaDetectionService.java` | Delete file | Remove field from ProductController, ProductServiceImpl |
| `LineaCost.java` | Delete file | Remove from ProductRepository (interface + impl), ProductService, ProductController, ProductServiceImpl |
| `LineaConverter.java` | Delete file | Remove from WebConfig |
| `lineas.html` | Delete file | Remove link from `dashboard.html` |
| Linea endpoints | Remove from ProductController | `GET /lineas`, `POST /lineas/actualizar-costo`, `POST /asignar-lineas-pendientes`, `GET /detectar-linea`, `GET /lineas-por-categoria` |
| Linea in `form.html` | Remove field + JS | Remove `linea` input, datalist, `detectarLinea()` JS, `/detectar-linea` fetch |
| `AdminFilter.java` | Remove paths | Remove `productos/lineas/*`, `asignar-lineas-pendientes`, `detectar-linea`, `lineas-por-categoria` |
| `findAllLineas()`, `updateLineaCost()`, `getLineaCosts()` | Remove from interface + impl | Also from controller + service |

## SQL Design for Dashboard

### Aggregate metrics (single row)

```sql
SELECT
  COALESCE(SUM(cost_price * stock), 0) AS total_inversion,
  COALESCE(SUM(price * stock), 0) AS total_venta,
  COALESCE(SUM((price - cost_price) * stock), 0) AS total_ganancia,
  COUNT(*) FILTER (WHERE cost_price > 0) AS count_con_costo,
  CASE WHEN COUNT(*) FILTER (WHERE cost_price > 0 AND price > 0) > 0
    THEN SUM(((price - cost_price) / NULLIF(price, 0)) * 100)
         FILTER (WHERE cost_price > 0 AND price > 0)
         / NULLIF(COUNT(*) FILTER (WHERE cost_price > 0), 0)
    ELSE 0 END AS margen_promedio
FROM products
WHERE 1=1
  /* + dynamic filters: name LIKE, category=, subcategory= */
```

### Top 20 products (chart data)

```sql
SELECT
  LEFT(name, 20) AS label,
  COALESCE((price - cost_price) * stock, 0) AS ganancia,
  COALESCE(cost_price * stock, 0) AS costo,
  COALESCE(price * stock, 0) AS venta,
  CASE WHEN price > 0 AND cost_price > 0
    THEN ((price - cost_price) / NULLIF(price, 0)) * 100
    ELSE 0 END AS margen
FROM products
WHERE stock > 0
  /* + dynamic filters */
ORDER BY ganancia DESC
LIMIT 20
```

### Category/Subcategory/Linea grouping

```sql
-- gananciaPorCategoria
SELECT COALESCE(category, 'SIN CAT') AS cat,
       SUM((price - cost_price) * stock) AS ganancia
FROM products WHERE 1=1 /* + filters */
GROUP BY category
ORDER BY ganancia DESC

-- gananciaPorLinea
SELECT COALESCE(NULLIF(linea, ''), 'SIN LINEA') AS linea,
       SUM((price - cost_price) * stock) AS ganancia
FROM products WHERE 1=1 /* + filters */
GROUP BY linea
ORDER BY ganancia DESC
```

## Data Flow

```
Controller (dashboard)
  │
  ├─→ repository.dashboardMetrics(name, cat, sub)
  │     → 1 SQL: aggregate metrics (totalInversion, margenPromedio, etc.)
  │     → returns Map<String, Object>
  │
  ├─→ repository.top20Products(name, cat, sub)
  │     → 1 SQL: top-20 chart lists
  │     → returns List<Object[]> → labels, ganancias, costos, ventas, margenes
  │
  ├─→ repository.profitByCategory(name, cat, sub)
  │     → 1 SQL: catLabels, catGanancias
  │
  └─→ repository.profitByLinea(name, cat, sub)
        → 1 SQL: lineaLabels, lineaGanancias
```

**Before**: 1 SQL (`SELECT *` + filter) + Java 440–505 loop → N products in heap.
**After**: 4 SQL aggregate queries → ~kilobytes of result, no heap pressure.

## Filter Query Builder

```java
private PreparedStatement buildFilterQuery(
    String selectClause,
    List<Object> params,
    String name, String category, String subCategory,
    boolean stockBajo
) {
    // SELECT {selectClause} FROM products WHERE 1=1
    // + dynamic AND LOWER(name) LIKE ?
    // + dynamic AND category = ?
    // + dynamic AND subcategory = ?
    // + dynamic AND stock BETWEEN 0 AND 1 (stockBajo only)
}
```

Applied to 5 methods: `countFiltered(→ SELECT COUNT(*))`, `sumInventario(→ SELECT COALESCE(SUM(price*stock),0))`, `sumStock(→ SELECT COALESCE(SUM(stock),0))`, `countSinStock(→ SELECT COUNT(*) FROM products WHERE stock=0 ...)`, `countStockBajo(→ SELECT COUNT(*) FROM products WHERE stock BETWEEN 0 AND 1 ...)`.

## File Changes

| File | Action | Detail |
|------|--------|--------|
| `mODEL/Product.java` | Modify | `Linea linea` → `String linea` |
| `mODEL/Linea.java` | Delete | Entire enum (~100 lines) |
| `mODEL/LineaCost.java` | Delete | Entire DTO |
| `service/LineaDetectionService.java` | Delete | Entire service |
| `service/ProductService.java` | Modify | Remove 3 linea methods, remove `linea` param from existing |
| `service/ProductServiceImpl.java` | Modify | Remove `@PostConstruct`, `lineaDetectionService`, auto-detect in save/update, 3 linea methods |
| `ProductoReposirotio/ProductRepository.java` | Modify | Remove `findAllLineas/updateLineaCost/getLineaCosts/existsByname/findAllPaged/countAll` |
| `ProductoReposirotio/impl/ProductRepositoryImpl.java` | Modify | Remove 6 methods, add `buildFilterQuery()` helper, add 4 dashboard SQL methods, refactor `mapResult()` |
| `conroller/ProductController.java` | Modify | Remove linea endpoints, autoDetectarLinea, all `Linea`/`LineaCost` refs; rewrite dashboard endpoint with SQL calls |
| `config/LineaConverter.java` | Delete | Converter |
| `config/WebConfig.java` | Modify | Remove `LineaConverter` injection + `addFormatters` override |
| `config/AdminFilter.java` | Modify | Remove 4 linea-related path checks |
| `Utils/Validates.java` | Delete | Dead utility |
| `templates/form.html` | Modify | Remove linea field + JS auto-detection |
| `templates/dashboard.html` | Modify | Remove "Líneas" quick-action link |
| `templates/lineas.html` | Delete | Entire template |

~18 files affected: 5 deleted, 1 created internally (builder), 12 modified.

## Deletion Order

1. Remove `Validates.java` — zero dependencies
2. Remove linea methods from `ProductRepository.java` interface (clean interface first)
3. Remove linea methods from `ProductRepositoryImpl.java`
4. Change `Product.linea` → `String`; update `mapResult()`, save/update SQL param
5. Remove `LineaDetectionService` refs from `ProductController`, `ProductServiceImpl`
6. Remove `Linea.java`, `LineaCost.java`, `LineaConverter.java`
7. Remove linea endpoints from `ProductController`
8. Update `WebConfig`, `AdminFilter`
9. Remove `@PostConstruct` from `ProductServiceImpl`
10. Remove dead methods and add `buildFilterQuery`
11. Rewrite dashboard SQL in controller — use new repository methods
12. Delete templates: `lineas.html`; update `form.html`, `dashboard.html`
13. Compile-check: `mvnw.cmd compile`
14. Test: `mvnw.cmd test`
15. Verify dashboard: compare `GET /productos/dashboard` output before/after deploy

## Migration Safety

| Check | Method |
|-------|--------|
| Numerical equality | Run Java-vs-SQL on same dataset (staging). Compare: totalInversion, totalVenta, totalGanancia, margenPromedio, top-20 values |
| Compilation | `mvnw.cmd compile` after each batch |
| Null linea handling | `String linea` defaults to `null` → use `NULLIF(linea, '')` in SQL |
| DB data preservation | No ALTER TABLE, no UPDATE — column `linea` stays untouched |
| Rollback | `git revert <hash>` — all code changes, no schema |

## Testing Strategy

| Layer | What | Approach |
|-------|------|----------|
| Unit | Dashboard SQL queries | Manual verification against staging data (no unit test infrastructure exists) |
| Compilation | All files compile | `mvnw.cmd compile` |
| Smoke | App starts without errors | `mvnw.cmd spring-boot:run` |
| Regression | Existing endpoints still work | Manual: product CRUD, movement, orders |

## Open Questions

- None — all decisions resolved by proposal + codebase analysis.
