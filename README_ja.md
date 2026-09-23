# 初期セットアップ

1. 資産生成
    - .\mvnw clean install
1. コンテナ構築
    - docker compose up -d --build

# コンテナ削除 → 再生成

    * docker compose -f .devcontainer/docker-compose.yml down -v
    * docker compose -f .devcontainer/docker-compose.yml up -d --build

# Java 資産の反映

    * docker restart my-tomcat

# SQL 反映(Flyway 起動)

    * docker compose run --rm flyway

# Playwrite のテスト実行

    * npx playwright test

# Prettier によるコード整形

初回のみ、プロジェクトの依存関係をインストールします。

```bash
npm install
```

現在 Prettier に対応している JSP を整形します。

```bash
npm run format:jsp
```

対応している JSP、JavaScript、CSS、Markdown、JSON、YAML を一括整形します。

```bash
npm run format
```

ファイルを変更せず、整形が必要か確認します。

```bash
npm run format:check
```

任意のファイルだけを整形する場合は、次のように実行します。

```bash
npx prettier --write "src/main/webapp/WEB-INF/jsp/security/auditLog.jsp"
```

JSP の整形には`prettier-plugin-jsp`を使用します。プラグインが解析できない旧形式の JSP は`.prettierignore`で除外しているため、構文を移行してから自動整形の対象へ追加してください。

# Flyway 実行

    * cd .devcontainer
    * docker compose up flyway
    or
    * docker compose down -v
    * docker compose up -d --build

# 修正後の資産適用

1. cd nwproject_docker
1. mvnw package
1. ブラウザ super reload

# VSCode キャッシュ先

    * %APPDATA%\Code\User\workspaceStorage

# My-AI との接続

cd C:\Users\tmng1\workspace\nwproject_docker

docker compose `  --env-file .env`
-f .devcontainer/docker-compose.yml `  -f .devcontainer/docker-compose.ai.yml`
up -d --build --force-recreate

-   war 再作成
    docker compose `  --env-file .env`
    -f .devcontainer/docker-compose.yml `  -f .devcontainer/docker-compose.ai.yml`
    run --rm -w /workspaces app ./mvnw clean package

-   tomcat 再構築
    docker compose `  --env-file .env`
    -f .devcontainer/docker-compose.yml `  -f .devcontainer/docker-compose.ai.yml`
    up -d --build --force-recreate

# 技術スタック

-   Java(Servlet/JSP)
-   Docker
-   Tomcat
-   MySQL
-   Flyway
-   GitHub Actions(CI/CD)
-   Playwright(E2E)
-   Google Compute Engine (GCE)
-   Nginx (Reverse Proxy)
-   HTTPS (TLS)
