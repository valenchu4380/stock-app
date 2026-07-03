---
name: test-and-verify
description: "Trigger: test, verify, error, bug, compilar, build, check. Run tests, check compilation, and find errors in the stock-app Spring Boot project."
license: Apache-2.0
metadata:
  author: "valentin"
  version: "1.0"
---

## Activation Contract

Activate when the user asks to:
- Run tests or check if tests pass
- Find compilation errors or build failures
- Verify the app has no static errors
- Add or fix tests

## Hard Rules

- Always run `mvnw.cmd clean compile` first to check compilation before running `mvnw.cmd test`.
- Never skip tests.
- Report ALL failures clearly: which file, which line, what error.

## Execution Steps

1. Run `mvnw.cmd clean compile` — check for compilation errors.
   - If compilation fails, report the errors and stop.
2. Run `mvnw.cmd test` — run the full test suite.
   - If tests fail, report which tests, what was expected vs actual.
3. If both pass, report success with a summary:
   - Compilation: OK
   - Tests: X passed, Y failed (0 if none)

## Output Contract

Return:
```
BUILD: ✅ / ❌
TESTS: ✅ / ❌ (N passed, M failed)
ERRORS: <file:line — desc> or "none"
```

## References

- `pom.xml` — Maven configuration (Java 21, Spring Boot 4.1.0)
- `mvnw.cmd` — Maven wrapper for Windows
