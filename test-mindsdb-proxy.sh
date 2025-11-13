#!/bin/bash

# MindsDB Proxy Test Script
# Tests the Spring Boot MindsDB proxy endpoints

BASE_URL="http://localhost:8080/api"
AUTH="admin:admin123"

echo "=== MindsDB Spring Boot Proxy Test ==="
echo ""

# Test 1: Health Check (requires auth)
echo "1. Testing MindsDB Health Check..."
echo "curl -u \$AUTH \$BASE_URL/mindsdb/health"
curl -s -u "$AUTH" "$BASE_URL/mindsdb/health" | python3 -m json.tool 2>/dev/null || echo "Authentication required or MindsDB not available"
echo ""
echo ""

# Test 2: List Databases
echo "2. Listing MindsDB Databases..."
echo "curl -u \$AUTH \$BASE_URL/mindsdb/databases"
curl -s -u "$AUTH" "$BASE_URL/mindsdb/databases" | python3 -m json.tool 2>/dev/null | head -30
echo ""
echo ""

# Test 3: List Models
echo "3. Listing MindsDB Models..."
echo "curl -u \$AUTH \$BASE_URL/mindsdb/models"
curl -s -u "$AUTH" "$BASE_URL/mindsdb/models" | python3 -m json.tool 2>/dev/null | head -30
echo ""
echo ""

# Test 4: Execute Custom Query
echo "4. Executing Custom Query (SHOW DATABASES)..."
echo 'curl -u $AUTH -X POST $BASE_URL/mindsdb/query -H "Content-Type: application/json" -d '"'"'{"query": "SHOW DATABASES"}'"'"''
curl -s -u "$AUTH" -X POST "$BASE_URL/mindsdb/query" \
  -H "Content-Type: application/json" \
  -d '{"query": "SHOW DATABASES"}' | python3 -m json.tool 2>/dev/null | head -30
echo ""
echo ""

# Test 5: Create Database Connection
echo "5. Creating MindsDB Database Connection to MySQL..."
QUERY='CREATE DATABASE email_db WITH ENGINE = '\''mysql'\'', PARAMETERS = {"host": "host.docker.internal", "port": 3306, "database": "email_reg_db", "user": "emailapp", "password": "emailapp123"}'
echo "curl -u \$AUTH -X POST \$BASE_URL/mindsdb/query -d '{\"query\": \"$QUERY\"}'"
curl -s -u "$AUTH" -X POST "$BASE_URL/mindsdb/query" \
  -H "Content-Type: application/json" \
  -d "{\"query\": \"$QUERY\"}" | python3 -m json.tool 2>/dev/null
echo ""
echo ""

# Test 6: Query Connected Database
echo "6. Querying Connected Database..."
curl -s -u "$AUTH" -X POST "$BASE_URL/mindsdb/query" \
  -H "Content-Type: application/json" \
  -d '{"query": "SELECT * FROM email_db.registered_emails LIMIT 5"}' | python3 -m json.tool 2>/dev/null | head -40
echo ""
echo ""

# Test 7: Check Fraud Detection (if model exists)
echo "7. Testing Fraud Detection..."
curl -s -u "$AUTH" -X POST "$BASE_URL/mindsdb/check-fraud" \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "username": "testuser"}' | python3 -m json.tool 2>/dev/null
echo ""
echo ""

# Test 8: Test without authentication (should fail)
echo "8. Testing without authentication (should return 401)..."
curl -s -w "\nHTTP Status: %{http_code}\n" "$BASE_URL/mindsdb/health" | head -5
echo ""

echo "=== Test Complete ==="
