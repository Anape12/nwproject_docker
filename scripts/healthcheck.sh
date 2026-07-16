#!/bin/bash

set -e

echo "Waiting for application..."

READY=false

for i in {1..60}; do
    if curl -kfs https://localhost/Login >/dev/null 2>&1; then
        READY=true
        break
    fi

    echo "Waiting... ($i/60)"
    sleep 2
done

if [ "$READY" = false ]; then

    echo "========================================"
    echo " Nginx Logs"
    echo "========================================"
    docker logs nginx --tail 100 || true

    echo "========================================"
    echo " Tomcat Logs"
    echo "========================================"
    docker logs my-tomcat --tail 200 || true

    echo "========================================"
    echo " MySQL Logs"
    echo "========================================"
    docker logs mysqldb --tail 100 || true

    echo "========================================"
    echo " Flyway Logs"
    echo "========================================"
    docker logs devcontainer-flyway-1 --tail 100 || true

    exit 1
fi

echo "========================================"
echo " Application Started Successfully"
echo "========================================"