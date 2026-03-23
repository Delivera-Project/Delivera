-- Organizaciones (tenant raíz)
CREATE TABLE organizations (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(255) NOT NULL,
    slug       VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);

-- Tipos de actividad empresarial
CREATE TYPE activity_type AS ENUM (
    'INDUSTRY', 'DISTRIBUTION', 'FOOD', 'RETAIL', 'TRANSPORT', 'OTHER'
);

-- Empresas dentro de una organización
CREATE TABLE companies (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID          NOT NULL REFERENCES organizations(id),
    name            VARCHAR(255)  NOT NULL,
    activity_type   activity_type NOT NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT now()
);

-- Roles de trabajador
CREATE TYPE worker_role AS ENUM (
    'COMPANY_ADMIN', 'ANALYST', 'OPERATOR'
);

-- Relación usuario ↔ empresa con rol
CREATE TABLE workers (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL REFERENCES users(id),
    company_id UUID        NOT NULL REFERENCES companies(id),
    role       worker_role NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT now(),
    UNIQUE (user_id, company_id)
);
