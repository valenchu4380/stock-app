# Tasks: optimizacion-recursos

## Change Overview

Refactor backend resource usage: remove unused linea system, eliminate startup scan, delete dead code.

## PR Plan (Feature Branch Chain)

| PR | Branch | Tasks | Status |
|----|--------|-------|--------|
| 1 | `feat/optimizacion-recursos-01-lineas` → `feat/optimizacion-recursos` | T1–T7 | ✅ **Done** |
| 2 | `feat/optimizacion-recursos-02-dashboard` → `feat/optimizacion-recursos-01-lineas` | T8–T10 | ✅ **Done** |
| 3 | `feat/optimizacion-recursos-03-consolidate` → `feat/optimizacion-recursos-02-dashboard` | T11–T13 | ✅ **Done** |

## PR 1 — Linea System Removal + Foundation

### T1 — Delete Validates.java
- [x] Delete `Utils/Validates.java`
- [x] Verify: grep for "Validates" returns 0
- Risk: Low

### T2 — Change Product.linea from Linea enum to String
- [x] `Product.java`: `Linea linea` → `String linea`
- [x] `ProductRepositoryImpl.java`: update `mapResult()`, `save()`, `update()`
- [x] Fix `findRelated()` references to `.getLinea().name()`
- Risk: Medium

### T3 — Remove 3 linea methods from ProductRepository interface
- [x] Remove `findAllLineas()`, `updateLineaCost()`, `getLineaCosts()`
- [x] Remove `Linea`, `LineaCost` imports
- Risk: Low

### T4 — Remove 3 linea implementations from ProductRepositoryImpl
- [x] Remove `findAllLineas()`, `updateLineaCost()`, `getLineaCosts()` implementations
- [x] Remove `Linea`, `LineaCost`, `Arrays` imports
- Risk: Low

### T5 — Remove from ProductService + ProductServiceImpl
- [x] Remove `lineaDetectionService` field
- [x] Remove `@PostConstruct void asignarLineasExistentes()`
- [x] Remove auto-detection in `save()` and `update()`
- [x] Remove `findAllLineas()`, `updateLineaCost()`, `getLineaCosts()` methods
- Risk: Medium

### T6 — Remove from ProductController
- [x] Remove `lineaDetectionService` injection
- [x] Remove `autoDetectarLinea()` private method
- [x] Remove endpoints: `/lineas`, `/lineas/actualizar-costo`, `/asignar-lineas-pendientes`, `/detectar-linea`, `/lineas-por-categoria`
- [x] Remove `Linea`/`LineaCost` model attr additions
- [x] Fix `.getDisplayName()` → String usage in dashboard and `generarDescripcion()`
- Risk: Medium

### T7 — Delete files and update config
- [x] Delete: `Linea.java`, `LineaCost.java`, `LineaDetectionService.java`, `LineaConverter.java`
- [x] Update `WebConfig.java`: remove LineaConverter injection + addFormatters
- [x] Update `AdminFilter.java`: remove linea-related path checks
- Risk: Medium

## PR 2 — Dashboard SQL Aggregation (Planned)

### T8 — Add dashboard SQL methods to repository
- [x] Add `dashboardMetrics()` aggregate query
- [x] Add `top20Products()` query
- [x] Add `profitByCategory()` query
- [x] Add `profitByLinea()` query
- Risk: High

### T9 — Rewrite dashboard endpoint
- [x] Replace Java loop with 4 SQL calls
- [x] Keep model attributes identical
- [x] Remove dead code paths
- Risk: High

### T10 — Remove linea-grouping from templates
- [x] Remove `lineaLabels`/`lineaGanancias` from dashboard template
- [x] Update `detalle.html` to remove linea references
- Risk: Medium

## PR 3 — Consolidate + Dead Code Removal (Planned)

### T11 — Add buildFilterQuery helper
- [x] Extract `buildFilterQuery()` private method
- [x] Refactor 5 filter methods to use it
- Risk: Medium

### T12 — Remove dead repository methods
- [x] Remove `existsByname()`
- [x] Remove `findAllPaged()`
- [x] Remove `countAll()`
- Risk: Low

### T13 — Compile-check and final verification
- [x] `mvnw.cmd compile` green
- [x] `mvnw.cmd test` green
- Risk: Low

## Review Workload Forecast

| PR | Changed Lines | Budget Status |
|----|--------------|---------------|
| PR 1 | ~375 deletions, ~10 insertions | ✅ Under 400 |
| PR 2 | ~150 insertions, ~100 deletions | ✅ Under 400 |
| PR 3 | ~100 insertions, ~80 deletions | ✅ Under 400 |

**Delivery Strategy**: feature-branch-chain
**Chain Strategy**: feature-branch-chain
