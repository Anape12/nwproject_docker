#!/bin/bash

set -e

URL="http://localhost:8080/Login"

MAX_RETRY=60
WAIT_SEC=2

for ((i=1; i<=MAX_RETRY; i++))
do
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$URL" || true)

    if [ "$STATUS" = "200" ]; then
        echo "Health Check OK"
        exit 0
    fi

    echo "Waiting... ($i/$MAX_RETRY)"

    sleep $WAIT_SEC
done

echo ""
echo "========================================"
echo " Health Check Failed"
echo "========================================"

echo ""
echo "===== Tomcat Logs ====="
docker logs my-tomcat --tail 200 || true

echo ""
echo "===== MySQL Logs ====="
docker logs mysqldb --tail 100 || true

echo ""
echo "===== Flyway Logs ====="
docker logs devcontainer-flyway-1 --tail 100 || true

exit 1