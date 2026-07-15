#!/bin/bash

set -e

cd ~/nwproject_docker

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

cd .devcontainer

docker compose up -d --build

echo "========================================"
echo " Health Check"
echo "========================================"

../scripts/healthcheck.sh

echo "========================================"
echo " Deploy Complete"
echo "========================================"