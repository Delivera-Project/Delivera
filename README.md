<p align="center">
  <img src=".github/assets/Delivera banner.png" alt="Delivera" />
</p>

Plataforma SaaS multi-tenant de gestión logística que centraliza pedidos y operaciones de múltiples empresas.

---

## Stack

| Capa | Tecnologías |
|---|---|
| Backend | Java 21, Spring Boot 3.2, Tomcat 10.1 (embedded), Lombok |
| ORM / Migraciones | Hibernate/JPA, Flyway (V32) |
| Base de datos | PostgreSQL 16 |
| Autenticación | Argon2 (Bouncy Castle), JWT HS256 (jjwt 0.12) |
| Frontend | Vue 3, Vite 7, Vue Router 4, Pinia, Vue i18n |
| Linting | ESLint, Oxlint |
| Infraestructura | Docker, docker-compose |
| Gestores de dependencias | Maven (backend), npm (frontend) |

## Estructura

```
delivera/
├── backend/          Spring Boot — API REST
├── frontend/         Vue 3 — SPA
├── docker/           docker-compose (PostgreSQL)
└── scripts/          Scripts de seed de base de datos
```

## Requisitos

- Java 21
- Maven
- Node.js >= 20
- Docker Desktop
- `jq` (para los scripts de seed)

## Arranque local

Abre tres consolas y ejecuta en este orden:

**1. Base de datos**

```bash
cd docker
docker compose up -d
```

**2. Backend**

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**3. Frontend**

```bash
cd frontend
npm install        # solo la primera vez
npm run dev
```

## Puertos

| Servicio | Puerto |
|---|---|
| PostgreSQL (host) | 5433 |
| Spring Boot | 8080 |
| Vite dev server | 3000 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |

El proxy de Vite reenvía `/api/*` al backend, por lo que no hace falta configurar CORS en desarrollo. Si cambias el puerto de Spring Boot, actualiza también el `target` del proxy en `vite.config.js`.

### Base de datos

Las credenciales están en dos sitios sincronizados:

- `docker/docker-compose.yml` → `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
- `backend/src/main/resources/application-dev.yml` → `spring.datasource`

## Seed de datos

Los scripts de seed se encuentran en `scripts/` y requieren que el backend esté arrancado:

```bash
# 1. Datos iniciales (admin + organización)
./scripts/seed-init.sh

# 2. Datos de demo encima
./scripts/seed-demo.sh
```

**Credenciales por defecto del seed:**

| Rol | Email | Contraseña |
|---|---|---|
| Admin empresa | `seed@delivera.com` | `Admin1234` |
| Usuario fidelizado | `user@delivera.com` | `User1234` |

Se pueden sobreescribir con variables de entorno:

```bash
ADMIN_EMAIL=otro@email.com ADMIN_PASSWORD=MiPass1 ./scripts/seed-init.sh
```

`seed-demo.sh` crea una organización con 3 empresas, 8 unidades operativas, 8 fidelizados y 9 pedidos en distintos estados.

## Tests

```bash
# Backend (H2 in-memory, no requiere PostgreSQL)
cd backend && mvn test

# Frontend — unitarios
cd frontend && npm run test:unit

# Frontend — E2E Playwright (no requiere backend)
cd frontend && npx playwright test
cd frontend && npx playwright test --grep @auth    # por tag
```

Tags E2E disponibles: `@auth` · `@navigation` · `@register` · `@profile` · `@units` · `@orders` · `@tracking`

## CI/CD

| Workflow | Cuándo se ejecuta |
|---|---|
| `ci.yml` | Push/PR a `main` o `develop` — build+test backend y frontend, Trivy IaC scan, SonarCloud + Quality Gate |
| `playwright.yml` | PR + `workflow_dispatch` — tests E2E en Chromium |
| `pr-title.yml` | PR — valida formato `tipo/descripcion` en el título |
| `pr-branch.yml` | PR — valida nombre de rama `tipo/descripcion` |
| `pr-commits.yml` | PR — valida mensajes de commit (Conventional Commits) |
| `release.yml` | `workflow_dispatch` manual — crea tag git + GitHub Release con changelog |

Dependabot revisa dependencias Maven, npm y GitHub Actions semanalmente y abre PRs automáticas.

## Producción

Activa el perfil `prod` con `SPRING_PROFILES_ACTIVE=prod`. Variables de entorno requeridas:

| Variable | Uso |
|---|---|
| `DATABASE_URL` | URL JDBC de PostgreSQL |
| `DATABASE_USER` | Usuario de la base de datos |
| `DATABASE_PASSWORD` | Contraseña de la base de datos |
| `JWT_SECRET` | Secret para firmar los tokens JWT |

## Migraciones

Flyway gestiona el esquema desde `backend/src/main/resources/db/migration/`. Versión actual: **V32**. Para añadir una nueva migración, crea un archivo `V{n}__descripcion.sql`.

| Rango | Contenido |
|---|---|
| V1–V6 | Infraestructura base de usuarios |
| V7–V18 | Multi-tenant, unidades operativas, pedidos y fidelizados |
| V19–V23 | Configuración extensible de estados, prioridades y roles de trabajador |
| V24–V26 | Renombrado `slug` → `handle`, relaxación de trigger de pedido, eliminación de enums |
| V27–V29 | FK de tipo de actividad a empresas, fidelizados many-to-many con empresas |
| V30–V32 | Tipo de pedido en órdenes, unicidad worker por empresa, limpieza `worker_role_config` |
