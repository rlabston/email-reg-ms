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

echo "=== Rebuilding Gateway ==="
cd ~/dev/mobile/spring/email-reg-ms
./gradlew :gateway:build -x test

echo "=== Restarting Gateway ==="
GATEWAY_PID=$(cat gateway.pid 2>/dev/null || echo "")
if [ -n "$GATEWAY_PID" ]; then
    echo "Stopping gateway PID $GATEWAY_PID"
    kill $GATEWAY_PID 2>/dev/null || true
    sleep 2
    kill -9 $GATEWAY_PID 2>/dev/null || true
fi

nohup ./gradlew :gateway:bootRun > gateway.log 2>&1 &
NEW_PID=$!
echo $NEW_PID > gateway.pid
echo "Gateway started with PID: $NEW_PID"

sleep 5
echo "=== Verifying Gateway ==="
curl -s http://localhost:8080/ | grep -q "html" && echo "✓ Gateway serving web app on http://localhost:8080" || echo "✗ Gateway check failed"

echo "=== Deployment Complete ==="
echo "Access the application at: http://localhost:8080"
