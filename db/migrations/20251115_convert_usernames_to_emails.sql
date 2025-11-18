-- Migration: convert usernames to registered emails
-- Date: 2025-11-15
-- Purpose: For UserAccount rows where username equals a RegisteredEmail.username,
-- replace the username with the canonical registered email (registered_emails.email).
-- WARNING: This will modify the `users.username` column which has a UNIQUE constraint.
-- If there are collisions (two different users mapping to the same email), the update
-- may fail or leave rows unchanged. Review the SELECT below first.

-- 1) Preview which rows would be updated
SELECT u.id AS user_id, u.username AS old_username, re.email AS new_username
FROM users u
JOIN registered_emails re ON u.username = re.username;

-- 2) Perform the update (uncomment to execute)
-- UPDATE users u
-- JOIN registered_emails re ON u.username = re.username
-- SET u.username = re.email;

-- 3) Verify the change
SELECT u.id, u.username, GROUP_CONCAT(r.role) AS roles
FROM users u
LEFT JOIN user_roles r ON u.id = r.user_id
GROUP BY u.id, u.username;
