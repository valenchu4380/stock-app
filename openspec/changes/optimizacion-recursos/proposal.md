# Proposal: Optimización de Recursos

## Intent
Reducir consumo de RAM en host de 1GB que se satura por cargar tablas completas en memoria Java. Simplificar la base de código eliminando el sistema de líneas (no utilizado) y código muerto.

## Scope

### In Scope
1. **Dashboard SQL**: Mover 6 métricas de agregación Java a PostgreSQL (SUM/AVG/COUNT)
2. **Eliminar sistema de líneas**: Linea enum, LineaDetectionService, LineaCost, LineaConverter, endpoints, auto-detection
3. **Eliminar @PostConstruct full scan**: startup ya no cargará todos los productos
4. **Dead code**: Validates.java, existsByname(), findAllPaged(), countAll()
5. **Consolidar 5 queries redundantes**: 1 query builder reutilizable en lugar de 5 copias

### Out of Scope
- Async startup (@Async) — innecesario sin linea detection
- PROMO dedup, Movement DRY — no solicitados
- Droppear columna `linea` en DB — se conserva para evitar pérdida de datos
- Configuración HikariCP, pool, JPA — no se toca

## Capabilities

### New Capabilities
None — refactor interno sin cambios de comportamiento visible.

### Modified Capabilities
None — pure refactor, no spec-level behavior changes. Outputs y endpoints existentes mantienen mismo formato.

## Approach

| # | Problema | Solución | Estrategia |
|---|----------|----------|------------|
| 1 | Dashboard carga todos los productos en Java para calcular métricas | SQL aggregation con SUM/AVG/COUNT + GROUP BY | Reemplazar getAllFiltered() + loop Java por 1-2 queries SQL |
| 2 | 5 queries redundantes por página con mismo WHERE | `buildFilterQuery(select, params)` reutilizable | Extraer builder → 6→2 roundtrips |
| 3 | Sistema líneas completo sin uso real | Eliminar todo el código Java (enum, service, cost, converter, endpoints) | Columna en DB intacta |
| 4 | @PostConstruct recorre todos los productos en startup | Eliminar método, sin reemplazo | Arranque inmediato sin carga masiva |
| 5 | Código muerto | Eliminar archivos/métodos no referenciados | Verificar con `mvnw.cmd compile` |

## Affected Areas

| File | Action |
|------|--------|
| `ProductController.java` | Modified: dashboard endpoint SQL, remove linea endpoints, remove autoDetectarLinea |
| `ProductServiceImpl.java` | Modified: remove @PostConstruct, remove linea references |
| `ProductRepository.java` / `ProductRepositoryImpl.java` | Modified: add aggregate queries, remove dead methods, add filter builder |
| `LineaDetectionService.java` | Deleted |
| `Linea.java` | Deleted |
| `LineaCost.java` | Deleted |
| `LineaConverter.java` | Deleted |
| `Validates.java` | Deleted |
| `producto/list.html` | Modified: remove linea column from table |
| DB schema | Unchanged — `linea` column preserved in PostgreSQL |

~10 files modified, ~6 deleted.

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Dashboard SQL aggregation returns different numbers | Medium | Compare Java vs SQL results on same dataset before deploying. Keep old code commented until verified. |
| Build breaks from deletion cascade (imports, references) | Low | `mvnw.cmd compile` after each deletion batch. Fix imports iteratively. |
| UI breaks for products with null linea | Low | Column exists in DB, data intact. UI table column removed only. No functional impact. |

## Rollback Plan
`git revert <commit-hash>` — todos los cambios son código únicamente. No hay migraciones de esquema, ni transformaciones de datos. Si las métricas del dashboard no coinciden, revertir, corregir la query, redeploy.

## Dependencies
None.

## Success Criteria
- [ ] Dashboard returns identical metrics (verified against current Java aggregation output)
- [ ] App compila (`mvnw.cmd compile`) e inicia sin errores
- [ ] No NullPointerException de referencias a linea eliminadas
- [ ] Tests existentes pasan (`mvnw.cmd test`)
- [ ] RAM en dashboard baja mediblemente (menos heap por request)
