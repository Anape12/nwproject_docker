#!/bin/bash

set -e

PROJECT_DIR="$HOME/nwproject_docker"

cd "$PROJECT_DIR"

chmod +x mvnw

echo "========================================"
echo " Test & Build"
echo "========================================"

time ./mvnw --no-transfer-progress clean package

echo "========================================"
echo " Docker Deploy"
echo "========================================"

cd "$PROJECT_DIR/.devcontainer"

# DBは停止・再作成せず、必要な場合だけ起動する。
docker compose up -d db

# 未適用のマイグレーションだけを実行する。
time docker compose run --rm flyway

# 初回、またはTomcat用Dockerfile/server.xmlが変わった場合だけイメージをビルドする。
DEPLOY_CACHE_DIR="$HOME/.cache/nwproject-deploy"
TOMCAT_HASH_FILE="$DEPLOY_CACHE_DIR/tomcat-image.sha256"
CURRENT_TOMCAT_HASH="$(sha256sum "$PROJECT_DIR/.devcontainer/Dockerfile.tomcat" "$PROJECT_DIR/server.xml" | sha256sum | cut -d' ' -f1)"
PREVIOUS_TOMCAT_HASH="$(cat "$TOMCAT_HASH_FILE" 2>/dev/null || true)"

if [ -z "$(docker compose images -q tomcat)" ] || [ "$CURRENT_TOMCAT_HASH" != "$PREVIOUS_TOMCAT_HASH" ]; then
    time docker compose build tomcat
    mkdir -p "$DEPLOY_CACHE_DIR"
    printf '%s' "$CURRENT_TOMCAT_HASH" > "$TOMCAT_HASH_FILE"
fi

# target/appはホストからマウントされるため、Tomcatだけ再作成すれば反映できる。
docker compose up -d --no-deps --force-recreate tomcat

# Nginxは起動していなければ起動し、稼働中なら再作成しない。
docker compose up -d --no-deps nginx

echo "========================================"
echo " Health Check"
echo "========================================"

time bash "$PROJECT_DIR/scripts/healthcheck.sh"

echo "========================================"
echo " Deploy Complete"
echo "========================================"
