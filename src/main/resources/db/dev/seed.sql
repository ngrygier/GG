INSERT INTO users (username, password_hash, email)
VALUES ('dev.jan', 'dev-only-not-a-real-hash', 'jan.nowak@dev.local'),
       ('dev.anna', 'dev-only-not-a-real-hash', 'anna.wisniewska@dev.local')
ON CONFLICT (username) DO NOTHING;

INSERT INTO employees (user_id, first_name, last_name, phone_number, hire_date)
SELECT u.user_id, 'Jan', 'Nowak', '500100200', DATE '2024-03-01'
FROM users u WHERE u.username = 'dev.jan'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO employees (user_id, first_name, last_name, phone_number, hire_date)
SELECT u.user_id, 'Anna', 'Wiśniewska', '500300400', DATE '2025-01-15'
FROM users u WHERE u.username = 'dev.anna'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO clients (first_name, last_name, company_name, email, phone_number)
SELECT 'Piotr', 'Kowalski', NULL, 'piotr.kowalski@dev.local', '600111222'
WHERE NOT EXISTS (SELECT 1 FROM clients WHERE email = 'piotr.kowalski@dev.local');

INSERT INTO clients (first_name, last_name, company_name, email, phone_number)
SELECT 'Maria', 'Zielińska', 'Zielińska Wnętrza Sp. z o.o.', 'biuro@zielinska-wnetrza.dev.local', '600333444'
WHERE NOT EXISTS (SELECT 1 FROM clients WHERE email = 'biuro@zielinska-wnetrza.dev.local');
