# PR 1/3: Remove Linea System + Foundation

Remove the unused `Linea` enum, `LineaDetectionService`, `LineaCost`, `LineaConverter`, and all related endpoints, methods, and configurations. No visible behavior changes.

## Changes

- **T1**: Delete dead `Validates.java` utility
- **T2**: Change `Product.linea` from `Linea` enum to `String` (DB column preserved)
- **T3**: Remove 3 linea methods from `ProductRepository` interface
- **T4**: Remove 3 linea implementations from `ProductRepositoryImpl`
- **T5**: Remove `lineaDetectionService`, `@PostConstruct`, auto-detection from `ProductService`
- **T6**: Remove 5 linea endpoints, `autoDetectarLinea()`, and `Linea`/`LineaCost` model attrs from `ProductController`
- **T7**: Delete `Linea.java`, `LineaCost.java`, `LineaDetectionService.java`, `LineaConverter.java`; update `WebConfig` and `AdminFilter`

## Verification

- ✅ `mvnw.cmd compile` — success
- ✅ `mvnw.cmd test` — 1/1 passed
- ✅ No `Linea` enum, `LineaCost`, `LineaDetectionService`, or `LineaConverter` imports remain

## Chain Context

| Field | Value |
|-------|-------|
| Chain | optimizacion-recursos |
| Tracker PR | feat/optimizacion-recursos (draft) |
| Position | 1 of 3 |
| Base | `feat/optimizacion-recursos` |
| Depends on | None |
| Follow-up | PR 2: Dashboard SQL aggregation |
| Review budget | ~375 deletions, ~10 insertions / 400 |
| Starts at | `main` @ ffda202 |
| Ends with | Linea system removed, foundation for dashboard SQL |

### Chain Overview

```
main
 └── feat/optimizacion-recursos              ← tracker (draft, no-merge)
      ↑ 📍 PR #1: feat/optimizacion-recursos-01-lineas  ← THIS PR
           └── PR #2: feat/optimizacion-recursos-02-dashboard
                └── PR #3: feat/optimizacion-recursos-03-consolidate
```

### Scope
- Includes: Linea system removal, type change, dead code deletion, config cleanup
- Excludes: Dashboard SQL rewrite, query builder extraction, dead API method removal

### Autonomy
- [x] CI is expected to pass for this PR branch
- [x] This PR has one deliverable scope
- [x] This PR can be rolled back without unrelated changes
- [x] Tests, docs, or manual verification cover this unit
