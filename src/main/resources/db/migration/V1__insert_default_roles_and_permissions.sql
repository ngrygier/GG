
INSERT INTO roles (role_id, role_name)
SELECT nextval('roles_role_id_seq'), role_name
FROM (VALUES
          ('OWNER'),
          ('OFFICE_WORKER'),
          ('PRODUCTION_WORKER')
     ) AS new_roles(role_name)
WHERE NOT EXISTS (
    SELECT 1
    FROM roles r
    WHERE r.role_name = new_roles.role_name
);


-- this is where we will be adding permissions

INSERT INTO permissions (permission_id, permission_name)
SELECT nextval('permissions_permission_id_seq'), permission_name
FROM (VALUES
          ('EMPLOYEE_READ'),
          ('EMPLOYEE_CREATE'),
          ('EMPLOYEE_UPDATE'),
          ('EMPLOYEE_DELETE'),
          ('CLIENT_READ'),
          ('CLIENT_CREATE'),
          ('CLIENT_UPDATE'),
          ('CLIENT_DELETE'),
          ('PROJECT_READ'),
          ('PROJECT_CREATE'),
          ('PROJECT_UPDATE'),
          ('TASK_READ'),
          ('TASK_UPDATE'),
          ('OFFER_READ'),
          ('OFFER_CREATE'),
          ('OFFER_UPDATE'),
          ('CONTRACT_READ')
     ) AS new_permissions(permission_name)
WHERE NOT EXISTS (
    SELECT 1
    FROM permissions p
    WHERE p.permission_name = new_permissions.permission_name
);

-- OWNER PERMISSIONS (ALL)

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
         CROSS JOIN permissions p
WHERE r.role_name = 'OWNER'
ON CONFLICT DO NOTHING;

-- OFFICE WORKER PERMISSIONS

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
         JOIN permissions p ON p.permission_name IN (
                                                     'EMPLOYEE_READ',
                                                     'CLIENT_READ',
                                                     'CLIENT_CREATE',
                                                     'CLIENT_UPDATE',
                                                     'CLIENT_DELETE',
                                                     'PROJECT_READ',
                                                     'PROJECT_CREATE',
                                                     'PROJECT_UPDATE',
                                                     'TASK_READ',
                                                     'OFFER_READ',
                                                     'OFFER_CREATE',
                                                     'OFFER_UPDATE',
                                                     'CONTRACT_READ'
    )
WHERE r.role_name = 'OFFICE_WORKER'
ON CONFLICT DO NOTHING;

-- PRODUCTION WORKER PERMISSIONS

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
         JOIN permissions p ON p.permission_name IN (
                                                     'CLIENT_READ',
                                                     'PROJECT_READ',
                                                     'PROJECT_UPDATE',
                                                     'TASK_READ'
    )
WHERE r.role_name = 'PRODUCTION_WORKER'
ON CONFLICT DO NOTHING;
