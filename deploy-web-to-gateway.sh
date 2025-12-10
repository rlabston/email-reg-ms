#!/bin/bash
# Deploy Angular web-frontend to the gateway

set -e

echo "=== Building Angular App ==="
cd ~/dev/mobile/spring/email-reg-ms/web-frontend
npm run build

echo "=== Copying to Gateway Static Resources ==="
rm -rf ~/dev/mobile/spring/email-reg-ms/gateway/src/main/resources/static/*
cp -r ~/dev/mobile/spring/email-reg-ms/web-frontend/dist/web-frontend/browser/* \
      ~/dev/mobile/spring/email-reg-ms/gateway/src/main/resources/static/

echo "=== Rebuilding Gateway JAR with clean build ==="
cd ~/dev/mobile/spring/email-reg-ms
./gradlew :gateway:clean :gateway:bootJar -x test

echo "=== Starting Backend and Gateway Services ==="
# Use start-dev.sh which handles both services properly
# It will:
# 1. Kill any existing processes on ports 8080 and 8081
# 2. Start backend on 8081 and wait for it to be ready
# 3. Start gateway on 8080 and wait for it to be ready
bash scripts/start-dev.sh

echo "=== Verifying Services ==="
curl -s http://localhost:8081/actuator/health | grep -q "UP" && echo "✓ Backend running on http://localhost:8081" || echo "✗ Backend check failed"
curl -s http://localhost:8080/ | grep -q "html" && echo "✓ Gateway serving web app on http://localhost:8080" || echo "✗ Gateway check failed"

echo "=== Deployment Complete ==="
echo "Access the application at: http://localhost:8080"
