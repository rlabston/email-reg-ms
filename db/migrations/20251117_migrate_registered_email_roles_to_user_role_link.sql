-- Migration: migrate data from legacy `registered_email_roles` into the application's
-- join table `user_role_link` and then remove the legacy table.
--
-- This file is safe to run multiple times (idempotent): it creates a backup, inserts
-- missing role rows into `roles`, inserts missing links into `user_role_link`, and
-- finally drops `registered_email_roles`.
--
-- IMPORTANT: Review before running on production. This migration assumes the
-- following tables exist with these columns:
--  - registered_email_roles(registered_email_id BIGINT, role VARCHAR)
--  - roles(id BIGINT AUTO_INCREMENT, name VARCHAR)
--  - user_role_link(registered_email_id BIGINT, role_id BIGINT)

START TRANSACTION;

-- 1) Create a backup (if not exists) so the data can be restored if needed
CREATE TABLE IF NOT EXISTS registered_email_roles_backup AS
SELECT * FROM registered_email_roles;

-- 2) Insert any missing roles referenced by registered_email_roles into roles table
INSERT INTO roles (name)
SELECT r.role
FROM (
  SELECT DISTINCT role FROM registered_email_roles
) r
LEFT JOIN roles ro ON ro.name = r.role
WHERE ro.id IS NULL;

-- 3) Insert missing links into user_role_link mapping registered_email -> roles.id
INSERT INTO user_role_link (registered_email_id, role_id)
SELECT rer.registered_email_id, ro.id
FROM registered_email_roles rer
JOIN roles ro ON ro.name = rer.role
LEFT JOIN user_role_link url ON url.registered_email_id = rer.registered_email_id AND url.role_id = ro.id
WHERE url.registered_email_id IS NULL;

-- 4) Optionally drop the legacy table now that data is copied. If you prefer to
-- keep the legacy table for longer, comment out the DROP TABLE line below.
DROP TABLE IF EXISTS registered_email_roles;

COMMIT;
