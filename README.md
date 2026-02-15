<p align="center">
  <img src=".github/assets/Delivera banner.png" alt="Delivera" />
</p>

Plataforma SaaS multi-tenant de gestión logística que centraliza pedidos y operaciones de múltiples empresas. 

---

## Stack

| Capa | Tecnologías |
|---|---|
| Backend | Java 21, Spring Boot 3.2, Tomcat 10.1 (embedded), Lombok |
| ORM / Migraciones | Hibernate/JPA, Flyway |
| Base de datos | PostgreSQL 16 (dev/prod), H2 (tests), HikariCP |
| Autenticación | BCrypt (spring-security-crypto), JWT HS256 (jjwt 0.12) |
| Frontend | Vue 3, Vite 7, Vue Router 4, Pinia |
| Testing | Vitest, Vue Test Utils |
| Linting / Formato | ESLint, Prettier, Oxlint |
| Infraestructura | Docker, docker-compose |
| Gestores de dependencias | Maven (backend), npm (frontend) |
| IDE | VSCode (recomendado) |

## Estructura

```
delivera/
├── backend/          Spring Boot — API REST
├── frontend/         Vue 3 — SPA
└── docker/           docker-compose (PostgreSQL)
```

## Requisitos

- Java 21
- Maven
- Node.js >= 20 (si tienes nvm, el `.nvmrc` del frontend te pone la versión)
- Docker Desktop

## Arranque local

Abre tres consolas y ejecuta esto en este orden:

**1. Base de datos**

```bash
cd docker
docker compose up -d
```

**2. Backend**

```bash
cd backend
mvn spring-boot:run
```

**3. Frontend**

```bash
cd frontend
npm install        # solo la primera vez
npm run dev
```

## Puertos

| Servicio | Puerto | Dónde cambiarlo |
|---|---|---|
| PostgreSQL (host) | 5433 | `docker/docker-compose.yml` → `ports` (el número de la izquierda) |
| Spring Boot | 8080 | `backend/src/main/resources/application-dev.yml` → añadir `server.port` |
| Vite dev server | 3000 | `frontend/vite.config.js` → `server.port` |

En desarrollo, Vite actúa de proxy y reenvía las peticiones a `/api/*` directamente al backend, así que no hace falta configurar nada de CORS. Si cambias el puerto de Spring Boot, actualiza también el `target` del proxy en `vite.config.js` para que apunte al nuevo puerto.

### Base de datos (nombre, usuario y contraseña)

Estos valores están en dos sitios:

- `docker/docker-compose.yml` → variables `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
- `backend/src/main/resources/application-dev.yml` → `spring.datasource` (url, username, password)

## API

### POST /api/auth/login

Recibe email y contraseña, y devuelve un JWT si las credenciales son correctas.

```json
// Request
{ "email": "admin@delivera.com", "password": "admin123" }

// Response 200
{ "email": "admin@delivera.com", "token": "<jwt>" }

// Response 401
{ "message": "Credenciales incorrectas" }
```

El token tiene una expiración de 24h. El secret para firmarlo está en `application-dev.yml`, bajo `app.jwt.secret`.

## Usuarios de ejemplo

Estos usuarios se crean automáticamente con la migración `V3__seed_users.sql` cuando se levanta la base de datos por primera vez.

| Email | Contraseña |
|---|---|
| admin@delivera.com | admin123 |
| usuario@delivera.com | usuario123 |

## Migraciones

Las migraciones las gestiona Flyway y están en `backend/src/main/resources/db/migration/`. Para añadir una nueva, crea un archivo con el nombre `V{n}__descripcion.sql`.

## Producción

Para producción, activa el perfil `prod` con `SPRING_PROFILES_ACTIVE=prod`. Estas variables de entorno tienen que estar definidas en el sistema y **no deben subirse al repositorio**:

| Variable | Uso |
|---|---|
| `DATABASE_URL` | URL JDBC de PostgreSQL |
| `DATABASE_USER` | Usuario de la base de datos |
| `DATABASE_PASSWORD` | Contraseña de la base de datos |
| `JWT_SECRET` | Secret para firmar los tokens JWT |

`application-prod.yml` las recoge automáticamente.
