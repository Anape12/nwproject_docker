#!/bin/bash

set -e

PROJECT_DIR="$HOME/nwproject_docker"

echo "Waiting for application..."

READY=false

for i in {1..60}; do
    if curl -fs http://localhost:8080/Login >/dev/null 2>&1; then
        READY=true
        break
    fi

    echo "Waiting... ($i/60)"
    sleep 2
done

if [ "$READY" = false ]; then

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