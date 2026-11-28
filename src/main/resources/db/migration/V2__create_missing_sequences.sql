CREATE SEQUENCE IF NOT EXISTS users_user_id_seq START WITH 1 INCREMENT BY 1;
SELECT setval('users_user_id_seq', COALESCE((SELECT MAX(user_id) FROM users), 1), EXISTS (SELECT 1 FROM users));

CREATE SEQUENCE IF NOT EXISTS employees_employee_id_seq START WITH 1 INCREMENT BY 1;
SELECT setval('employees_employee_id_seq', COALESCE((SELECT MAX(employee_id) FROM employees), 1), EXISTS (SELECT 1 FROM employees));

CREATE SEQUENCE IF NOT EXISTS clients_client_id_seq START WITH 1 INCREMENT BY 1;
SELECT setval('clients_client_id_seq', COALESCE((SELECT MAX(client_id) FROM clients), 1), EXISTS (SELECT 1 FROM clients));

CREATE SEQUENCE IF NOT EXISTS offers_offer_id_seq START WITH 1 INCREMENT BY 1;
SELECT setval('offers_offer_id_seq', COALESCE((SELECT MAX(offer_id) FROM offers), 1), EXISTS (SELECT 1 FROM offers));

CREATE SEQUENCE IF NOT EXISTS contracts_contract_id_seq START WITH 1 INCREMENT BY 1;
SELECT setval('contracts_contract_id_seq', COALESCE((SELECT MAX(contract_id) FROM contracts), 1), EXISTS (SELECT 1 FROM contracts));

CREATE SEQUENCE IF NOT EXISTS designer_commissions_commission_id_seq START WITH 1 INCREMENT BY 1;
SELECT setval('designer_commissions_commission_id_seq', COALESCE((SELECT MAX(commission_id) FROM designer_commissions), 1), EXISTS (SELECT 1 FROM designer_commissions));
