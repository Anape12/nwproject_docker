#!/bin/bash

set -e

PROJECT_DIR="$HOME/nwproject_docker"

cd "$PROJECT_DIR"

echo "========================================"
echo " Git Synchronize"
echo "========================================"

git fetch origin
git reset --hard origin/main

chmod +x mvnw

echo "========================================"
echo " Unit Test"
echo "========================================"

./mvnw test

echo "========================================"
echo " Build"
echo "========================================"

./mvnw clean install -DskipTests

echo "========================================"
echo " Docker Deploy"
echo "========================================"

cd "$PROJECT_DIR/.devcontainer"

docker compose up -d --build

echo "========================================"
echo " Health Check"
echo "========================================"

bash "$PROJECT_DIR/scripts/healthcheck.sh"

echo "========================================"
echo " Deploy Complete"
echo "========================================"