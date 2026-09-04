# 初期セットアップ

1. 資産生成
   - .\mvnw clean install
1. コンテナ構築
   - docker compose up -d --build

# コンテナ削除→再生成

    * docker compose -f .devcontainer/docker-compose.yml down -v
    * docker compose -f .devcontainer/docker-compose.yml up -d --build

# Java資産の反映

    * docker restart my-tomcat

# SQL反映(Flyway起動)

    * docker compose run --rm flyway

# Playwriteのテスト実行

    * npx playwright test

# Flyway実行

    * cd .devcontainer
    * docker compose up flyway
    or
    * docker compose down -v
    * docker compose up -d --build

# 修正後の資産適用

1. cd nwproject_docker
1. mvnw package
1. ブラウザ super reload

# VSCodeキャッシュ先

    * %APPDATA%\Code\User\workspaceStorage

# My-AIとの接続

cd C:\Users\tmng1\workspace\nwproject_docker

docker compose `  --env-file .env`
-f .devcontainer/docker-compose.yml `  -f .devcontainer/docker-compose.ai.yml`
up -d --build --force-recreate

- war再作成
  docker compose `  --env-file .env`
  -f .devcontainer/docker-compose.yml `  -f .devcontainer/docker-compose.ai.yml`
  run --rm -w /workspaces app ./mvnw clean package

- tomcat再構築
  docker compose `  --env-file .env`
  -f .devcontainer/docker-compose.yml `  -f .devcontainer/docker-compose.ai.yml`
  up -d --build --force-recreate

# 技術スタック

- Java(Servlet/JSP)
- Docker
- Tomcat
- MySQL
- Flyway
- GitHub Actions(CI/CD)
- Playwright(E2E)
- Google Compute Engine (GCE)
- Nginx (Reverse Proxy)
- HTTPS (TLS)
