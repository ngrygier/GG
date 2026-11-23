CREATE TABLE users (
    user_id       SERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email         VARCHAR(100) NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT users_username_key UNIQUE (username),
    CONSTRAINT users_email_key UNIQUE (email)
);

CREATE TABLE roles (
    role_id   SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    CONSTRAINT roles_role_name_key UNIQUE (role_name)
);

CREATE TABLE permissions (
    permission_id   SERIAL PRIMARY KEY,
    permission_name VARCHAR(100) NOT NULL,
    CONSTRAINT permissions_permission_name_key UNIQUE (permission_name)
);

CREATE TABLE user_roles (
    user_id INTEGER NOT NULL REFERENCES users (user_id),
    role_id INTEGER NOT NULL REFERENCES roles (role_id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE role_permissions (
    role_id       INTEGER NOT NULL REFERENCES roles (role_id),
    permission_id INTEGER NOT NULL REFERENCES permissions (permission_id),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE employees (
    employee_id  SERIAL PRIMARY KEY,
    user_id      INTEGER     NOT NULL REFERENCES users (user_id) ON DELETE RESTRICT,
    first_name   VARCHAR(50) NOT NULL,
    last_name    VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    hire_date    DATE        NOT NULL,
    CONSTRAINT employees_user_id_key UNIQUE (user_id)
);

CREATE TABLE clients (
    client_id    SERIAL PRIMARY KEY,
    first_name   VARCHAR(50),
    last_name    VARCHAR(50),
    company_name VARCHAR(150),
    email        VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE client_addresses (
    address_id       SERIAL PRIMARY KEY,
    client_id        INTEGER      NOT NULL REFERENCES clients (client_id),
    street           VARCHAR(100) NOT NULL,
    building_number  VARCHAR(10)  NOT NULL,
    apartment_number VARCHAR(10),
    postal_code      VARCHAR(10)  NOT NULL,
    city             VARCHAR(100) NOT NULL
);

CREATE TABLE designers (
    designer_id  SERIAL PRIMARY KEY,
    first_name   VARCHAR(50)  NOT NULL,
    last_name    VARCHAR(50)  NOT NULL,
    phone_number VARCHAR(20)  NOT NULL,
    email        VARCHAR(100) NOT NULL
);

CREATE TABLE designer_commissions (
    commission_id    SERIAL PRIMARY KEY,
    designer_id      INTEGER        NOT NULL REFERENCES designers (designer_id),
    client_id        INTEGER        NOT NULL REFERENCES clients (client_id),
    commission_amout NUMERIC(12, 2) NOT NULL,
    status           VARCHAR(30)    NOT NULL,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_designer_client UNIQUE (designer_id, client_id)
);

CREATE TABLE offers (
    offer_id     SERIAL PRIMARY KEY,
    client_id    INTEGER        NOT NULL REFERENCES clients (client_id),
    created_by   INTEGER        NOT NULL REFERENCES employees (employee_id) ON DELETE RESTRICT,
    offer_number VARCHAR(50)    NOT NULL,
    offer_date   DATE           NOT NULL,
    valid_until  DATE           NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    status       VARCHAR(30)    NOT NULL,
    description  TEXT,
    CONSTRAINT offers_offer_number_key UNIQUE (offer_number)
);

CREATE TABLE projects (
    project_id                    SERIAL PRIMARY KEY,
    client_id                     INTEGER      NOT NULL REFERENCES clients (client_id),
    offer_id                      INTEGER REFERENCES offers (offer_id),
    project_name                  VARCHAR(150) NOT NULL,
    description                   TEXT,
    status                        VARCHAR(30)  NOT NULL,
    start_date                    DATE,
    end_date                      DATE,
    installation_street           VARCHAR(100) NOT NULL,
    installation_building_number  VARCHAR(10)  NOT NULL,
    installation_apartment_number VARCHAR(10),
    installation_postal_code      VARCHAR(10)  NOT NULL,
    installation_city             VARCHAR(100) NOT NULL
);

CREATE TABLE project_employees (
    project_id  INTEGER NOT NULL REFERENCES projects (project_id),
    employee_id INTEGER NOT NULL REFERENCES employees (employee_id),
    PRIMARY KEY (project_id, employee_id)
);

CREATE TABLE contracts (
    contract_id     SERIAL PRIMARY KEY,
    project_id      INTEGER        NOT NULL REFERENCES projects (project_id),
    contract_number VARCHAR(50)    NOT NULL,
    signed_date     DATE           NOT NULL,
    contract_value  NUMERIC(12, 2) NOT NULL,
    status          VARCHAR(30)    NOT NULL,
    document_path   VARCHAR(255),
    CONSTRAINT contracts_project_id_key UNIQUE (project_id),
    CONSTRAINT contracts_contract_number_key UNIQUE (contract_number)
);

CREATE TABLE tasks (
    task_id      SERIAL PRIMARY KEY,
    project_id   INTEGER REFERENCES projects (project_id),
    assigned_to  INTEGER      NOT NULL REFERENCES employees (employee_id),
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    status       VARCHAR(30)  NOT NULL,
    priority     VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date     DATE,
    completed_at TIMESTAMPTZ
);
