# Internal Architecture — Delta Spec

## Purpose

Refactor backend resource usage: move aggregation from Java to SQL, remove unused linea system, eliminate startup scan, delete dead code, consolidate redundant queries. Zero visible behavior changes.

## REQUIREMENTS

### R1: Dashboard SQL Aggregation

The dashboard endpoint MUST compute 6 metrics via SQL aggregation (SUM/AVG/COUNT/GROUP BY) instead of loading all products into Java memory.

**Before**: `productService.getAllFiltered()` returns List<Product> → Java loops compute totals, categorías, líneas, top-20 sorting, margin averages.

**After**: 2-3 aggregate SQL queries compute all metrics server-side. Only top-20 product data is loaded as rows.

**Metrics** (must be identical):
- `totalInversion` → `SELECT SUM(cost_price * stock)`
- `totalVenta` → `SELECT SUM(price * stock)`
- `totalGanancia` → `SELECT SUM((price - cost_price) * stock)`
- `margenPromedio` → `SELECT AVG(((price - cost_price) / price) * 100) WHERE price > 0 AND cost_price > 0`
- `gananciaPorCat` → `SELECT category, SUM((price - cost_price) * stock) GROUP BY category`
- Top-20 products → `SELECT ... ORDER BY ((price - cost_price) * stock) DESC LIMIT 20`
- `gananciaPorLinea` → `SELECT linea, SUM((price - cost_price) * stock) GROUP BY linea`

Edge case: empty results MUST return 0 / empty lists, not nulls. Products with cost_price=0 MUST be excluded from margin average denominator.

### R2: Remove Linea System

ALL linea-related code MUST be deleted:
- `Linea.java` enum (101 lines, 66 values)
- `LineaDetectionService.java` (123 lines, regex pattern matching)
- `LineaCost.java` model
- `LineaConverter.java` converter
- `ProductController.java` endpoints: `/lineas`, `/lineas/actualizar-costo`, `/asignar-lineas-pendientes`, `/detectar-linea`, `/lineas-por-categoria`
- `autoDetectarLinea()` private method in controller
- All `lineaDetectionService` injections and calls
- `ProductService.findAllLineas()`, `updateLineaCost()`, `getLineaCosts()` + repo impls
- `ProductRepository.findAllLineas()`, `updateLineaCost()`, `getLineaCosts()` + impl
- Template `lineas.html`
- `WebConfig.java` — remove `LineaConverter` injection + `addFormatters` registration
- Linea filter param from product listing endpoints (keep `linea` column in DB only)

Post-removal: `Product.linea` field stays (DB column preserved, no schema migration). `mapResult()` in repo SHOULD set `linea = null` always. `detalle.html` references to `product.linea` MUST be removed (line 148-159, 457-459). `generarDescripcion()` MUST drop linea mentions. `findRelated()` MUST drop linea-based ordering.

Edge case: null-safe. No code path should reference `Linea` enum, `LineaCost`, or `LineaDetectionService` after removal.

### R3: Remove @PostConstruct

The `@PostConstruct void asignarLineasExistentes()` in `ProductServiceImpl` MUST be deleted. No replacement. App MUST start without loading all products from DB.

Edge case: any code depending on startup linea assignment (none exist — linea system is unused) MUST not break.

### R4: Delete Dead Code

The following MUST be deleted (unreferenced):
- `Validates.java` — entire file (3 unused validation methods)
- `existsByname(String name)` — interface + impl method (unused, `existsBynameAndSubCategory` remains)
- `findAllPaged(int offset, int limit)` — interface + impl method (unused, `findAllPagedFiltered` remains)
- `countAll()` — interface + impl method (unused, `countFiltered` remains)

Edge case: compilation MUST succeed. `mvnw.cmd compile` after each deletion.

### R5: Consolidate Redundant Queries

Extract `buildFilterQuery(String selectClause, List<Object> params, String name, String category, String subCategory, String linea, boolean stockBajo)` as a reusable private method in `ProductRepositoryImpl`.

**Currently**: 5 methods duplicate identical WHERE-building logic:
- `findAllPagedFiltered()` — SELECT * with ORDER + LIMIT
- `countFiltered()` — SELECT COUNT(*)
- `sumInventario()` — SELECT COALESCE(SUM(price*stock),0)
- `sumStock()` — SELECT COALESCE(SUM(stock),0)
- `countSinStock()` — SELECT COUNT(*) with stock=0 base

**After**: Each method calls `buildFilterQuery()` + appends its specific clause. `countStockBajo()` already has a different base WHERE (`stock >= 0 AND stock <= 1`) — leave as-is.

Edge case: ALL filtered methods MUST return identical results compared to current implementation. Verified by running with same params.

## ACCEPTANCE CRITERIA

| ID | Criterion | Verification |
|----|-----------|-------------|
| AC1 | Dashboard 6 metrics match Java aggregation | Compare output on same dataset, 3 filter combinations (empty, category filter, name filter) |
| AC2 | App compiles without linea references | `mvnw.cmd compile` succeeds |
| AC3 | App starts without @PostConstruct | `mvnw.cmd spring-boot:run` starts, no lazy-init errors |
| AC4 | No dead code remains | `mvnw.cmd compile` + grep for Validates/existsByname/findAllPaged/countAll yields 0 |
| AC5 | 5 queries reduced to 1 builder + 5 calls | Code review: no duplicate WHERE building |
| AC6 | Templates render without linea | `/productos`, `/productos/dashboard`, `/productos/detalle/...` load without error |
| AC7 | Existing test passes | `mvnw.cmd test` green |
