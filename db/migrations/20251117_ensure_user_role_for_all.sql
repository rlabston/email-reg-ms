-- Ensure every registered_emails row has a USER role in user_role_link
-- Idempotent: safe to run multiple times

START TRANSACTION;

-- 1) Ensure a canonical USER role exists in the roles table
INSERT INTO roles (name)
SELECT 'USER' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'USER');

-- 2) Insert a mapping into user_role_link for any registered_emails that do not
-- yet have the USER role.
INSERT INTO user_role_link (registered_email_id, role_id)
SELECT re.id, r.id
FROM registered_emails re
CROSS JOIN roles r
WHERE r.name = 'USER'
  AND NOT EXISTS (
    SELECT 1 FROM user_role_link url WHERE url.registered_email_id = re.id AND url.role_id = r.id
  );

COMMIT;
