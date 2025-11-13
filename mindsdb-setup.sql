-- MindsDB Setup for Email Registration System
-- Run these commands in the MindsDB web UI (SQL Editor)

-- Step 1: Connect to your MySQL email registration database
CREATE DATABASE email_db
WITH ENGINE = 'mysql',
PARAMETERS = {
    "host": "host.docker.internal",
    "port": 3306,
    "database": "email_reg_db",
    "user": "emailapp",
    "password": "emailapp123"
};

-- Step 2: Verify connection by querying registered emails
SELECT * FROM email_db.registered_emails LIMIT 5;

-- Step 3: Create a predictor for fraud detection
-- This model learns patterns from existing registrations to detect suspicious activity
CREATE MODEL fraud_detector
FROM email_db (
    SELECT 
        email,
        username,
        registration_date,
        CASE 
            WHEN email LIKE '%temp%' OR email LIKE '%fake%' THEN 1
            WHEN username LIKE '%test%' OR username LIKE '%admin%' THEN 1
            ELSE 0
        END AS is_suspicious
    FROM registered_emails
)
PREDICT is_suspicious;

-- Step 4: Wait for training to complete, then check status
SELECT status FROM mindsdb.models WHERE name = 'fraud_detector';

-- Step 5: Use the model to predict if new registrations are suspicious
SELECT 
    email,
    username,
    is_suspicious,
    is_suspicious_confidence
FROM fraud_detector
WHERE email = 'newuser@example.com' 
AND username = 'newuser';

-- Step 6: Create a real-time view joining live data with predictions
CREATE VIEW suspicious_registrations AS
SELECT 
    r.id,
    r.email,
    r.username,
    r.registration_date,
    p.is_suspicious,
    p.is_suspicious_confidence
FROM email_db.registered_emails r
JOIN fraud_detector p
WHERE p.is_suspicious = 1
AND p.is_suspicious_confidence > 0.7;

-- Step 7: Query suspicious registrations
SELECT * FROM suspicious_registrations;

-- Additional Examples:

-- Email domain analysis
SELECT 
    SUBSTRING_INDEX(email, '@', -1) as domain,
    COUNT(*) as registration_count
FROM email_db.registered_emails
GROUP BY domain
ORDER BY registration_count DESC;

-- Time-based registration patterns
SELECT 
    DATE(registration_date) as reg_date,
    COUNT(*) as daily_registrations
FROM email_db.registered_emails
GROUP BY DATE(registration_date)
ORDER BY reg_date DESC;

-- Predict user engagement (if you add activity tracking later)
-- CREATE MODEL engagement_predictor
-- FROM email_db (
--     SELECT 
--         username,
--         registration_date,
--         last_login_date,
--         login_count,
--         DATEDIFF(NOW(), last_login_date) as days_since_login
--     FROM user_activity
-- )
-- PREDICT days_since_login;
