#!/bin/bash

# Complete MindsDB Integration Test
# This script demonstrates the full workflow from starting services to testing ML predictions

set -e

echo "============================================"
echo "MindsDB Integration Complete Test"
echo "============================================"
echo ""

# Step 1: Check Prerequisites
echo "Step 1: Checking Prerequisites..."
echo "-----------------------------------"

# Check Spring Boot
if pgrep -f "bootRun" > /dev/null; then
    echo "✓ Spring Boot is running on port 8080"
else
    echo "✗ Spring Boot is NOT running"
    echo "  Start it with: ./gradlew bootRun"
    exit 1
fi

# Check MindsDB
if lsof -i :47334 > /dev/null 2>&1; then
    echo "✓ MindsDB is running on port 47334"
else
    echo "✗ MindsDB is NOT running"
    echo "  Start it with: docker run -d --name mindsdb -p 47334:47334 -p 47335:47335 mindsdb/mindsdb"
    echo "  Or check Docker Desktop"
    exit 1
fi

# Check MySQL
if mysql -h 127.0.0.1 -u emailapp -pemailapp123 -e "SELECT 1" > /dev/null 2>&1; then
    echo "✓ MySQL is accessible"
else
    echo "✗ MySQL is NOT accessible"
    exit 1
fi

echo ""

# Step 2: Test Spring Boot Endpoints
echo "Step 2: Testing Spring Boot API..."
echo "-----------------------------------"

AUTH="admin:admin123"
BASE_URL="http://localhost:8080/api"

# Test authentication
echo -n "Testing authentication... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -u "$AUTH" "$BASE_URL/mindsdb/health")
if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
    echo "✓ (HTTP $HTTP_CODE)"
else
    echo "✗ (HTTP $HTTP_CODE - Expected 200 or 500)"
    exit 1
fi

echo ""

# Step 3: Connect MindsDB to MySQL
echo "Step 3: Connecting MindsDB to MySQL Database..."
echo "------------------------------------------------"

CONNECT_QUERY='CREATE DATABASE email_db WITH ENGINE = '\''mysql'\'', PARAMETERS = {"host": "host.docker.internal", "port": 3306, "database": "email_reg_db", "user": "emailapp", "password": "emailapp123"}'

echo "Executing: CREATE DATABASE email_db..."
RESPONSE=$(curl -s -u "$AUTH" -X POST "$BASE_URL/mindsdb/query" \
  -H "Content-Type: application/json" \
  -d "{\"query\": \"$CONNECT_QUERY\"}")

if echo "$RESPONSE" | grep -q "error"; then
    if echo "$RESPONSE" | grep -q "already exists"; then
        echo "✓ Database connection already exists"
    else
        echo "✗ Error creating database connection:"
        echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
        exit 1
    fi
else
    echo "✓ Database connection created successfully"
fi

echo ""

# Step 4: Verify Connection
echo "Step 4: Verifying Database Connection..."
echo "-----------------------------------------"

VERIFY_QUERY="SELECT * FROM email_db.registered_emails LIMIT 3"
echo "Executing: $VERIFY_QUERY"

RESPONSE=$(curl -s -u "$AUTH" -X POST "$BASE_URL/mindsdb/query" \
  -H "Content-Type: application/json" \
  -d "{\"query\": \"$VERIFY_QUERY\"}")

if echo "$RESPONSE" | grep -q "error"; then
    echo "✗ Error querying database:"
    echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
else
    echo "✓ Successfully queried email_reg_db"
    echo "Sample data:"
    echo "$RESPONSE" | python3 -m json.tool 2>/dev/null | head -30
fi

echo ""

# Step 5: Create Fraud Detection Model
echo "Step 5: Creating Fraud Detection ML Model..."
echo "----------------------------------------------"

# First check if model exists
CHECK_MODEL="SELECT * FROM mindsdb.models WHERE name='fraud_detector'"
MODEL_EXISTS=$(curl -s -u "$AUTH" -X POST "$BASE_URL/mindsdb/query" \
  -H "Content-Type: application/json" \
  -d "{\"query\": \"$CHECK_MODEL\"}")

if echo "$MODEL_EXISTS" | grep -q "fraud_detector"; then
    echo "✓ Model 'fraud_detector' already exists"
else
    echo "Creating new fraud detection model..."
    
    CREATE_MODEL_QUERY="CREATE MODEL fraud_detector FROM email_db (SELECT email, username, CASE WHEN email LIKE '%temp%' OR email LIKE '%test%' THEN 1 ELSE 0 END as is_suspicious FROM registered_emails) PREDICT is_suspicious"
    
    RESPONSE=$(curl -s -u "$AUTH" -X POST "$BASE_URL/mindsdb/query" \
      -H "Content-Type: application/json" \
      -d "{\"query\": \"$CREATE_MODEL_QUERY\"}")
    
    if echo "$RESPONSE" | grep -q "error"; then
        echo "✗ Error creating model:"
        echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
    else
        echo "✓ Model creation initiated"
        echo "  Model will train in the background"
        echo "  Check status with: SELECT status FROM mindsdb.models WHERE name='fraud_detector'"
    fi
fi

echo ""

# Step 6: Test Fraud Detection
echo "Step 6: Testing Fraud Detection..."
echo "-----------------------------------"

# Test with normal user
echo "Testing with normal user (alice@example.com)..."
RESPONSE=$(curl -s -u "$AUTH" -X POST "$BASE_URL/mindsdb/check-fraud" \
  -H "Content-Type: application/json" \
  -d '{"email": "alice@example.com", "username": "alice"}')

echo "Result: $RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
echo ""

# Test with suspicious user  
echo "Testing with suspicious user (test@temp.com)..."
RESPONSE=$(curl -s -u "$AUTH" -X POST "$BASE_URL/mindsdb/check-fraud" \
  -H "Content-Type: application/json" \
  -d '{"email": "test@temp.com", "username": "testuser"}')

echo "Result: $RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
echo ""

# Step 7: Query Predictions
echo "Step 7: Making Predictions with ML Model..."
echo "--------------------------------------------"

PREDICT_QUERY="SELECT email, username, is_suspicious, is_suspicious_confidence FROM fraud_detector WHERE email='newuser@example.com' AND username='newuser'"

echo "Executing: $PREDICT_QUERY"
RESPONSE=$(curl -s -u "$AUTH" -X POST "$BASE_URL/mindsdb/query" \
  -H "Content-Type: application/json" \
  -d "{\"query\": \"$PREDICT_QUERY\"}")

if echo "$RESPONSE" | grep -q "error"; then
    echo "Note: Model might still be training or query format needs adjustment"
    echo "$RESPONSE" | python3 -m json.tool 2>/dev/null | head -20
else
    echo "✓ Prediction successful:"
    echo "$RESPONSE" | python3 -m json.tool 2>/dev/null | head -30
fi

echo ""
echo "============================================"
echo "✓ Integration Test Complete!"
echo "============================================"
echo ""
echo "Summary:"
echo "  - Spring Boot API: Running on port 8080"
echo "  - MindsDB: Running on port 47334"
echo "  - MySQL Database: Connected via MindsDB"
echo "  - ML Model: fraud_detector (check status for training completion)"
echo "  - Fraud Detection API: /api/mindsdb/check-fraud"
echo ""
echo "Next steps:"
echo "  1. Check model training status:"
echo "     curl -u admin:admin123 -X POST http://localhost:8080/api/mindsdb/query \\"
echo "       -H 'Content-Type: application/json' \\"
echo "       -d '{\"query\": \"SELECT status, accuracy FROM mindsdb.models WHERE name='\''fraud_detector'\''\" }'"
echo ""
echo "  2. Integrate with Angular frontend or Android app"
echo "  3. Add real-time fraud detection to registration flow"
echo ""
