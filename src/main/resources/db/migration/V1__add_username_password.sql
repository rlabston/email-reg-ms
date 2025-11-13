-- Add username and password columns if they don't exist (MySQL 8+ syntax)
ALTER TABLE registered_emails ADD COLUMN IF NOT EXISTS username VARCHAR(255);
ALTER TABLE registered_emails ADD COLUMN IF NOT EXISTS password VARCHAR(255);
