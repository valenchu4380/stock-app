# Exploration: Restructurar Proyecto

## Current State

The project (`stock-app`) is a Spring Boot 4.1.0 + Thymeleaf + PostgreSQL application for product inventory management. It has evolved organically — the shopping cart was recently migrated from a separate page to a floating overlay on the index, and many other changes have accumulated without systematic cleanup.

### Source structure (49 Java files, 10 templates, 1 CSS, 1 JS, 1 test)

```
src/main/java/com/valentin/tu_cv_spring_bot/
├── TuCvSpringBotApplication.java
└── TuCv/
    ├── config/        — WebConfig, AdminFilter, LineaConverter
    ├── conroller/     — (typo) AdminAuthController, ProductController, RootController
    ├── Exception/     — (uppercase) ProductNotFoundException, InvalidProductException
    ├── mODEL/         — (weird casing) Product, Orden, Movement, Linea, enums, etc.
    ├── ProductoReposirotio/ — (typo+Spanish) interfaces + impl/ subpackage
    ├── service/       — ProductService, OrdenService, MovementService (+Impl), LineaDetectionService, ProductValidator
    └── Utils/         — (uppercase) Validates.java
```

### Templates (Thymeleaf, all Spanish content)
- `index.html` — Main product listing + cart overlay (large, ~900 lines, all CSS inline)
- `carrito.html` — Old full-page cart (likely dead code since cart is now overlay)
- `detalle.html` — Product detail with related products, promo banner
- `form.html` — Product create/edit form
- `dashboard.html` — Financial dashboard with Chart.js charts (~890 lines)
- `compras.html` — Orders management table
- `lineas.html` — Line cost management
- `movements.html` — Stock movement history
- `admin-login.html` — Simple login form
- `fragments/producto-cards.html` — Card grid fragment
- `fragments/toast.html` — Toast/dialog fragment

### Static assets
- `css/main.css` — Shared base styles (~150 lines)
- `js/carrito-compartido.js` — Shared cart functions (~150 lines)
- `favicon.ico`

### Test
- `TuCvSpringBotApplicationTests.java` — Default context load test only

---

## Affected Areas

- `src/main/java/com/valentin/tu_cv_spring_bot/TuCv/` — Entire package tree needs restructuring
- `src/main/java/com/valentin/tu_cv_spring_bot/TuCv/conroller/` — Typo in package name
- `src/main/java/com/valentin/tu_cv_spring_bot/TuCv/ProductoReposirotio/` — Typo + Spanish package name
- `src/main/java/com/valentin/tu_cv_spring_bot/TuCv/mODEL/` — Inconsistent casing
- `src/main/java/com/valentin/tu_cv_spring_bot/TuCv/Exception/` — Uppercase package
- `src/main/java/com/valentin/tu_cv_spring_bot/TuCv/Utils/` — Uppercase package
- `src/main/java/com/valentin/tu_cv_spring_bot/TuCv/Utils/Validates.java` — Possibly unused class
- `src/main/java/com/valentin/tu_cv_spring_bot/TuCv/conroller/RootController.java` — Trivial class, could merge
- `src/main/java/com/valentin/tu_cv_spring_bot/TuCv/conroller/ProductController.java` — Lines 635-639: `/carrito` endpoint for carrito.html
- `src/main/java/com/valentin/tu_cv_spring_bot/TuCv/service/ProductService.java` — Line 20: unused method `actualizarpricesPorCategoria()`
- `src/main/java/com/valentin/tu_cv_spring_bot/TuCv/service/ProductServiceImpl.java` — Lines 77-86 and 115-122: duplicated autoDetectarLinea logic
- `src/main/java/com/valentin/tu_cv_spring_bot/TuCv/ProductoReposirotio/impl/ProductRepositoryImpl.java` — ~8 duplicated SQL WHERE clause builders
- `src/main/resources/templates/carrito.html` — Dead template (cart now in overlay)
- `src/main/resources/templates/index.html` — Has PROMO defined inline (duplicated in carrito.html, detalle.html, and carrito-compartido.js)
- `src/main/resources/templates/detalle.html` — Has PROMO defined inline
- `src/main/resources/static/js/carrito-compartido.js` — Has PROMO defined (but templates override it)
- `src/main/resources/application.properties` — Hardcoded DB credentials
- `src/main/resources/application-local.properties` — Hardcoded admin secret
- `pom.xml` — Dependency formatting is messy

---

## Key Issues Found

1. **Package naming chaos** — `conroller` (typo), `ProductoReposirotio` (typo), `mODEL` (weird casing), `Exception` (uppercase), `Utils` (uppercase). All need normalization to standard Java lowercase conventions.

2. **Extra nesting layer** — All classes sit under `TuCv/` subpackage, making paths like `com.valentin.tu_cv_spring_bot.TuCv.conroller.ProductController`. This is redundant — could be `com.valentin.tu_cv_spring_bot.controller.ProductController`.

3. **Dead code: carrito.html** — The old cart page template is still served at `/productos/carrito`. Since cart is now a floating overlay on `index.html`, this template and endpoint are orphaned.

4. **Duplicated auto-detection logic** — `autoDetectarLinea()` appears in both `ProductController` (line 152) and `ProductServiceImpl` (lines 77-86, 115-122) with the same implementation.

5. **Duplicated PROMO config** — The gift promotion object (`PROMO`) is defined in 4 places: `carrito-compartido.js` (canonical), `index.html`, `detalle.html`, and `carrito.html`.

6. **Duplicate SQL WHERE clause building** — The same filter logic repeated across ~8 methods in `ProductRepositoryImpl` (findAllFiltered, findAllPagedFiltered, countFiltered, countStockBajo, sumInventario, sumStock, countSinStock).

7. **Unused class: Validates.java** — `Utils/Validates.java` appears to be unused. `ProductValidator` handles actual validation.

8. **Unused interface method: `actualizarpricesPorCategoria()`** — Declared in `ProductService` and `ProductRepository` but never called from any controller.

9. **Import typo: `tools.jackson`** — `OrdenServiceImpl.java` uses `import tools.jackson...` instead of `com.fasterxml.jackson...`

10. **Variable naming inconsistencies** — `Categorys`/`SubCategorys` (incorrect English plural), `massage` instead of `message` in Validates, mixed Spanish/English (`getByname`, `actualizarpricesPorSubCategoria`).

11. **Security concern** — Hardcoded database credentials and admin secret in `application.properties` and `application-local.properties`.

12. **No separation of layout** — All templates have inline `<style>` blocks instead of using shared CSS or Thymeleaf layout dialect.

13. **Minimal test coverage** — Only 1 autogenerated test file exists.

14. **Inline JavaScript duplication** — `WHATSAPP_NUM`, cart functions, and `subPorCat` mapping defined both inline (index.html) and in shared JS (carrito-compartido.js).

---

## Approaches

### 1. Light restructure — Fix naming, remove dead code

Fix package name typos and casing, delete clearly orphaned code, update imports.

- Rename packages: `conroller` → `controller`, `ProductoReposirotio` → `repository`, `mODEL` → `model`, `Exception` → `exception`, `Utils` → `util`
- Remove `carrito.html` and the `/productos/carrito` endpoint
- Remove `Validates.java` (unused)
- Remove unused interface method `actualizarpricesPorCategoria()`
- Fix `tools.jackson` → `com.fasterxml.jackson`
- Update all imports and package declarations

**Pros:** Low risk, immediately improves navigability, simple mechanical changes  
**Cons:** Doesn't address duplicated logic or SQL redundancy, extra `TuCv/` nesting stays  
**Effort:** Low  

### 2. Medium restructure — Consolidation + cleanup

Everything from Approach 1, plus:
- Consolidate `autoDetectarLinea()` — keep only in service, remove from controller
- Extract SQL WHERE clause builder into a helper method in `ProductRepositoryImpl`
- Move PROMO config + cart JS out of templates into `carrito-compartido.js` only
- Fix naming: `getByname` → `getByName`, `Categorys` → `categories`
- Optionally flatten `TuCv/` nesting if desired
- Remove `RootController` and configure redirect in `WebConfig`

**Pros:** Addresses real code quality issues, reduces duplication, easier maintenance  
**Cons:** More files changed, needs careful refactoring of SQL methods  
**Effort:** Medium  

### 3. Full restructure — Modernize the architecture

Everything from Approach 2, plus:
- Migrate from raw JDBC to `JdbcTemplate` (or Spring Data JPA)
- Extract DTOs and a proper service/controller boundary
- Use Thymeleaf layout dialect for template inheritance (remove inline CSS)
- Add integration tests for repositories and services
- Externalize credentials to environment variables only
- Use `@ExceptionHandler` for global error handling instead of try/catch in controller

**Pros:** Modern, maintainable, testable, follows Spring best practices  
**Cons:** High effort, significant refactoring risk (especially JPA migration), could introduce regressions  
**Effort:** High  

---

## Recommendation

**Approach 2 (Medium restructure)** is the sweet spot for the user's request to "summarize and structure better." It:

1. Fixes the glaring package naming issues that make the project look unprofessional
2. Removes dead code that causes confusion (carrito.html, Validates.java)
3. Consolidates duplicated business logic (linea detection, PROMO config)
4. Reduces SQL maintenance burden with helper methods
5. Does NOT require a full framework migration (low risk)
6. Can be done incrementally without breaking functionality

Approach 3 would be ideal long-term but is overkill for a "summarize and structure better" request — it's a full rewrite disguised as a restructure.

---

## Risks

- **Import hell** — Package renames will touch 49 Java files. Automated refactoring tools should handle this, but manual verification of each import is needed.
- **template references** — Controller route changes (if any) must match template names; removing `carrito.html` needs the `/productos/carrito` endpoint removed too.
- **Testing gap** — With only 1 test, any regression from refactoring won't be caught. Manual testing on the live app is essential.
- **team coordination** — If others work on the same branch, package renames will cause merge conflicts.
- **DB credentials** — While not strictly a restructuring issue, the hardcoded credentials should be called out separately.

---

## Ready for Proposal

Yes
