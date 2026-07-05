# Stock App — gestión de inventario para perfumería

Aplicación web Spring Boot para administrar productos, stock, precios y ventas de un negocio de perfumería y cosmética. Catálogo público con carrito flotante + panel admin protegido.

## Quick start

```bash
# 1. Clonar y entrar
git clone <repo>
cd tu-cv-spring-bot

# 2. Variables de entorno (Railway)
set DB_URL=jdbc:postgresql://<host>:<port>/<db>
set DB_USERNAME=postgres
set DB_PASSWORD=<password>
set ADMIN_SECRET=<tu-clave-admin>

# 3. Compilar y arrancar
mvn spring-boot:run
```

Abrir [http://localhost:8080/productos](http://localhost:8080/productos).

## Stack

| Capa | Tecnología |
|------|------------|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Frontend | Thymeleaf + CSS nativo + JavaScript |
| Base de datos | PostgreSQL (Railway) |
| Build | Maven |
| Acceso a datos | JDBC directo con `DataSource` |

## Estructura del proyecto

```
src/main/java/com/valentin/tu_cv_spring_bot/
├── TuCv/
│   ├── conroller/          # Controladores web
│   │   ├── AdminAuthController.java   — Login/logout admin
│   │   ├── ProductController.java     — CRUD, carrito, dashboard, movimientos
│   │   └── RootController.java        — Redirección raíz
│   ├── service/            # Lógica de negocio
│   │   ├── ProductService / Impl      — CRUD productos, stock, precios
│   │   ├── OrdenService / Impl        — Órdenes de compra
│   │   ├── MovementService / Impl     — Historial de movimientos
│   │   ├── LineaDetectionService      — Asignación automática de líneas
│   │   └── ProductValidator           — Validación de campos
│   ├── ProductoReposirotio/           — Repositorios (JDBC directo)
│   │   ├── ProductRepository.java     — Interface
│   │   ├── impl/ProductRepositoryImpl — SQL nativo
│   │   ├── OrdenRepository / Impl     — Órdenes
│   │   └── MovementRepository / Impl  — Movimientos
│   ├── mODEL/              — Entidades de dominio
│   │   ├── Product.java
│   │   ├── Orden.java, OrdenItem.java
│   │   ├── Movement.java
│   │   ├── Linea.java, LineaCost.java
│   │   ├── ProductCategory.java       — Enum
│   │   └── SubCategory.java           — Enum
│   ├── config/             — Configuración
│   │   ├── WebConfig.java             — CORS, view controllers
│   │   ├── AdminFilter.java           — Filtro de autenticación admin
│   │   └── LineaConverter.java        — Conversor Thymeleaf
│   ├── Exception/          — Excepciones custom
│   └── Utils/              — Utilidades (Validates.java, no usado)
└── TuCvSpringBotApplication.java

src/main/resources/
├── templates/              — Thymeleaf
│   ├── index.html          — Catálogo público con carrito flotante
│   ├── detalle.html        — Detalle de producto + WhatsApp
│   ├── form.html           — Formulario alta/edición (admin)
│   ├── admin-login.html    — Login admin
│   ├── dashboard.html      — Panel con gráficos y métricas
│   ├── compras.html        — Gestión de órdenes (admin)
│   ├── movements.html      — Historial de movimientos (admin)
│   ├── lineas.html         — Costos por línea (admin)
│   ├── carrito.html        — Página carrito (en desuso)
│   └── fragments/          — Fragmentos reutilizables
│       ├── toast.html
│       └── producto-cards.html
├── static/
│   ├── css/main.css
│   └── js/carrito-compartido.js
└── application.properties
```

## Features

| Feature | Ruta | Acceso |
|---------|------|--------|
| Catálogo de productos | `/productos` | Público |
| Búsqueda | `/productos/buscar` | Público |
| Detalle + WhatsApp | `/productos/detalle/{name}/{sub}` | Público |
| Carrito flotante | overlay en index.html | Público |
| Órdenes de compra | vía carrito | Público |
| Admin — productos | `/productos/nuevo`, `/editar`, `/eliminar` | Admin |
| Admin — stock | `/productos/ajustar-stock` | Admin |
| Admin — precios | `/productos/actualizar-precios-sub` | Admin |
| Admin — dashboard | `/productos/dashboard` | Admin |
| Admin — movimientos | `/productos/movimientos` | Admin |
| Admin — líneas | `/productos/lineas` | Admin |
| Admin — órdenes | `/productos/compras` | Admin |

## Configuración

| Variable | Requerida | Default |
|----------|-----------|---------|
| `DB_URL` | Sí | — |
| `DB_USERNAME` | No | `postgres` |
| `DB_PASSWORD` | Sí | — |
| `ADMIN_SECRET` | No | vacío (sin protección) |
| `whatsapp.number` | No | `543854202134` |
| `server.port` | No | `8080` |

> ⚠️ Sin `ADMIN_SECRET` el panel admin está abierto. Es obligatorio para producción.

## Admin auth

El panel admin usa autenticación por cookie:

1. `POST /admin/auth` con `password=...` → valida contra `ADMIN_SECRET`
2. Setea cookie `admin_token` con SHA-256 del secreto
3. `AdminFilter` intercepta rutas `/productos/nuevo`, `/editar`, `/eliminar`, etc.
4. `GET /admin/logout` elimina la cookie

## Checklist — puesta en marcha

- [ ] Variables de entorno configuradas (DB_URL, DB_PASSWORD, ADMIN_SECRET)
- [ ] Base de datos PostgreSQL disponible con schema creado
- [ ] `mvn clean compile` sin errores
- [ ] `mvn spring-boot:run` levanta sin excepciones
- [ ] Puerto 8080 accesible
- [ ] Admin login funcional con `ADMIN_SECRET`
- [ ] Catálogo público muestra productos

## Estado del proyecto

Proyecto funcional en producción con cambios acumulados. Pendientes de resolver:

- [ ] Migrar a Spring Data JPA para eliminar JDBC directo
- [ ] Agregar tests unitarios (actualmente hay 1 test)
- [ ] Mover schema migrations a Flyway
- [ ] Unificar case-sensitivity en queries (LOWER consistente)
- [ ] Proteger admin POST con CSRF (SameSite aplicado, falta token)
