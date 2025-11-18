-- Migration: merge users/roles into registered_emails
-- Date: 2025-11-15
-- 1) Create a new roles table for registered_emails
CREATE TABLE IF NOT EXISTS registered_email_roles (
  registered_email_id BIGINT NOT NULL,
  role VARCHAR(255) NOT NULL,
  CONSTRAINT fk_reg_email_roles_reg FOREIGN KEY (registered_email_id) REFERENCES registered_emails(id)
);

-- 2) Copy existing roles where users.username matches registered_emails.email
INSERT INTO registered_email_roles (registered_email_id, role)
SELECT re.id AS registered_email_id, r.role
FROM users u
JOIN user_roles r ON u.id = r.user_id
JOIN registered_emails re ON u.username = re.email
ON DUPLICATE KEY UPDATE role = VALUES(role);

-- 3) Optional: verify mapping (run manually)
SELECT re.id, re.email, GROUP_CONCAT(r.role) AS roles
FROM registered_emails re
LEFT JOIN registered_email_roles r ON re.id = r.registered_email_id
GROUP BY re.id, re.email;

-- Note: We do not drop the original `users` or `user_roles` tables in this migration
-- to preserve auditability. After verifying the application works with
-- `registered_email_roles`, you can drop the old tables in a later safe migration.
