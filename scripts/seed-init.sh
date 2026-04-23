#!/usr/bin/env bash
# seed-init.sh — Datos iniciales de Delivera
#
# Limpia la base de datos y crea el usuario administrador con su organización y empresa.
# Los tipos de actividad se insertan automáticamente via Flyway (V18).
#
# Uso:
#   ./scripts/seed-init.sh
#   ADMIN_EMAIL=otro@email.com ADMIN_PASSWORD=OtraPass1 ./scripts/seed-init.sh
#
# Requisitos: curl, jq, docker (para la limpieza de BD)

set -euo pipefail

API="${API_URL:-http://localhost:8080/api/v1}"
ADMIN_EMAIL="${ADMIN_EMAIL:-}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-}"
GLOBAL_ADMIN_EMAIL="${GLOBAL_ADMIN_EMAIL:-}"
GLOBAL_ADMIN_PASSWORD="${GLOBAL_ADMIN_PASSWORD:-}"
DB_CONTAINER="${DB_CONTAINER:-delivera_postgres}"
DB_NAME="${DB_NAME:-delivera_dev}"
DB_USER="${DB_USER:-delivera_user}"

if [[ -z "$ADMIN_EMAIL" || -z "$ADMIN_PASSWORD" || -z "$GLOBAL_ADMIN_EMAIL" || -z "$GLOBAL_ADMIN_PASSWORD" ]]; then
  echo "ERROR: faltan variables de entorno obligatorias."
  echo "       Exporta ADMIN_EMAIL, ADMIN_PASSWORD, GLOBAL_ADMIN_EMAIL, GLOBAL_ADMIN_PASSWORD antes de ejecutar."
  exit 1
fi

# ── Comprobaciones previas ─────────────────────────────────────────────────────

if ! command -v jq &> /dev/null; then
  echo "ERROR: 'jq' no está instalado. Instálalo con: apt install jq / brew install jq"
  exit 1
fi

echo "=== Delivera — seed-init ==="
echo "Backend:    $API"
echo "Admin:      $ADMIN_EMAIL"
echo ""

if ! curl -sf "$API/activity-types" > /dev/null 2>&1; then
  echo "ERROR: El backend no responde en $API"
  echo "       Asegúrate de que Spring Boot esté arrancado."
  exit 1
fi

# ── Limpieza de base de datos ──────────────────────────────────────────────────

echo "Limpiando base de datos..."

docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -c \
  "TRUNCATE TABLE order_events, orders, loyal_users, workers, operational_units, companies, organizations, users RESTART IDENTITY CASCADE;" \
  > /dev/null

echo "OK — Base de datos limpia."
echo ""

# ── Crear administrador + organización + empresa ───────────────────────────────

echo "Creando usuario administrador..."

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API/auth/register/company" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$ADMIN_EMAIL\",
    \"password\": \"$ADMIN_PASSWORD\",
    \"username\": \"seed_admin\",
    \"firstName\": \"Seed\",
    \"lastName\": \"Admin\",
    \"orgName\": \"Delivera Seed\",
    \"orgHandle\": \"delivera-seed\",
    \"companyName\": \"Empresa Principal\",
    \"activityType\": \"DISTRIBUTION\"
  }")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "201" ]; then
  echo "OK — Administrador creado."
else
  echo "ERROR ($HTTP_CODE): $BODY"
  exit 1
fi

# ── Crear GLOBAL_ADMIN ────────────────────────────────────────────────────────

echo "Registrando usuario global admin..."

GLOBAL_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$GLOBAL_ADMIN_EMAIL\",
    \"username\": \"global_admin\",
    \"firstName\": \"Global\",
    \"lastName\": \"Admin\",
    \"password\": \"$GLOBAL_ADMIN_PASSWORD\"
  }")

GLOBAL_HTTP=$(echo "$GLOBAL_RESPONSE" | tail -n1)
if [[ "$GLOBAL_HTTP" != "201" ]]; then
  echo "ERROR registrando global admin ($GLOBAL_HTTP): $(echo "$GLOBAL_RESPONSE" | sed '$d')"
  exit 1
fi
echo "OK — Usuario admin@delivera.com creado."

echo "Asignando rol GLOBAL_ADMIN en BD..."
docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -c \
  "INSERT INTO workers (user_id, company_id, role)
   SELECT u.id, c.id, 'GLOBAL_ADMIN'
   FROM users u
   JOIN companies c ON TRUE
   WHERE u.email = '$GLOBAL_ADMIN_EMAIL'
   LIMIT 1;" \
  > /dev/null
echo "OK — Rol GLOBAL_ADMIN asignado."

# ── Resumen ────────────────────────────────────────────────────────────────────

echo ""
echo "=== Listo ==="
echo ""
echo "  Organización: Delivera Seed (handle: delivera-seed)"
echo "  Empresa:      Empresa Principal"
echo "  Admin:        $ADMIN_EMAIL"
echo "  Global admin: $GLOBAL_ADMIN_EMAIL"
echo ""
echo "  Tipos de actividad: gestionados por Flyway (V18)"
echo ""
echo "Para cargar datos de demo: ./scripts/seed-demo.sh"
