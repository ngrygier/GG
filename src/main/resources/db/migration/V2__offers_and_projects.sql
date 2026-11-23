-- ===================== offers =====================
ALTER TABLE offers ALTER COLUMN created_by DROP NOT NULL;
ALTER TABLE offers ALTER COLUMN offer_number DROP NOT NULL;
ALTER TABLE offers ALTER COLUMN valid_until DROP NOT NULL;
ALTER TABLE offers ALTER COLUMN total_amount DROP NOT NULL;

ALTER TABLE offers DROP COLUMN status;

ALTER TABLE offers RENAME COLUMN offer_date TO submission_date;
ALTER TABLE offers RENAME COLUMN description TO order_description;
ALTER TABLE offers ALTER COLUMN order_description SET NOT NULL;

ALTER TABLE offers ADD COLUMN first_contact_date DATE        NOT NULL;
ALTER TABLE offers ADD COLUMN deadline           DATE;
ALTER TABLE offers ADD COLUMN accepted           BOOLEAN     NOT NULL DEFAULT FALSE;
ALTER TABLE offers ADD COLUMN created_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE offers ADD COLUMN updated_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE offers ADD CONSTRAINT offers_submission_after_first_contact_chk
    CHECK (submission_date >= first_contact_date);
ALTER TABLE offers ADD CONSTRAINT offers_deadline_after_submission_chk
    CHECK (deadline IS NULL OR deadline >= submission_date);

CREATE INDEX offers_client_id_idx ON offers (client_id);

-- ===================== projects =====================
ALTER TABLE projects ALTER COLUMN installation_street DROP NOT NULL;
ALTER TABLE projects ALTER COLUMN installation_building_number DROP NOT NULL;
ALTER TABLE projects ALTER COLUMN installation_postal_code DROP NOT NULL;
ALTER TABLE projects ALTER COLUMN installation_city DROP NOT NULL;

ALTER TABLE projects RENAME COLUMN description TO order_description;

ALTER TABLE projects ALTER COLUMN offer_id SET NOT NULL;
ALTER TABLE projects DROP CONSTRAINT projects_offer_id_fkey;
ALTER TABLE projects ADD CONSTRAINT projects_offer_id_fkey
    FOREIGN KEY (offer_id) REFERENCES offers (offer_id) ON DELETE CASCADE;

ALTER TABLE projects ALTER COLUMN status SET DEFAULT 'PENDING';
ALTER TABLE projects ADD CONSTRAINT projects_status_chk
    CHECK (status IN ('PENDING', 'IN_PROGRESS', 'PRODUCTION', 'COMPLETED'));

ALTER TABLE projects ADD COLUMN client_source VARCHAR(100);
ALTER TABLE projects ADD COLUMN deadline      DATE;
ALTER TABLE projects ADD COLUMN created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE projects ADD COLUMN updated_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX projects_offer_id_idx ON projects (offer_id);
CREATE INDEX projects_client_id_idx ON projects (client_id);
CREATE INDEX projects_status_idx ON projects (status);

-- ===================== project_employees =====================
ALTER TABLE project_employees DROP CONSTRAINT project_employees_project_id_fkey;
ALTER TABLE project_employees ADD CONSTRAINT project_employees_project_id_fkey
    FOREIGN KEY (project_id) REFERENCES projects (project_id) ON DELETE CASCADE;

CREATE INDEX project_employees_employee_id_idx ON project_employees (employee_id);
