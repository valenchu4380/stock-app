# Bug Fixes — Admin

## Objective

Fix two bugs in admin templates: the encoding issue in `admin-login.html` where `Contraseña` renders incorrectly, and the undefined JavaScript variable `brandGanancias` in `dashboard.html` that causes a console error on page load.

## Requirements

### Requirement: Contraseña displays correctly in admin-login

The password label in `admin-login.html` SHALL render "Contraseña" (with correct ñ) on all pages. The HTML `Content-Type` meta tag SHALL declare `charset=UTF-8`. If the incorrect text uses HTML entities, they SHALL be replaced with the literal character or correct entity (`&ntilde;`).

#### Scenario: UTF-8 charset is declared

- GIVEN `admin-login.html`
- WHEN the `<head>` section is inspected
- THEN a `<meta charset="UTF-8">` or `<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">` SHALL be present

#### Scenario: Contraseña renders as text

- GIVEN `admin-login.html` loaded in a browser
- WHEN the password label text is read
- THEN it SHALL display "Contraseña" (literal ñ or `&ntilde;`)
- AND no garbled/mojibake characters SHALL appear

### Requirement: brandGanancias is defined before use in dashboard

In `dashboard.html`, the variable `brandGanancias` SHALL be defined and initialized before any code that references it. It SHALL NOT be `undefined` at the time of first use. The fix SHALL involve declaring and initializing the variable, or moving the reference after the definition.

#### Scenario: Dashboard loads without console error

- GIVEN `dashboard.html`
- WHEN the page loads in a browser
- THEN the browser console SHALL NOT show `Uncaught ReferenceError: brandGanancias is not defined` or similar

#### Scenario: brandGanancias has a value

- GIVEN `dashboard.html`
- WHEN `brandGanancias` is inspected in the browser console after page load
- THEN it SHALL be defined (not `undefined`) and SHALL contain a numeric or string value

## Verification Criteria

- Manual: load `admin-login` page — verify "Contraseña" displays correctly
- Manual: load `dashboard` page — open browser console — verify no `ReferenceError` for `brandGanancias`
- Manual: type `brandGanancias` in console — verify it returns a value
- Automated: `mvnw.cmd test` — all existing tests pass

## Affected Templates

`templates/admin-login.html`, `templates/dashboard.html`
