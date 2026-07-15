# 初期セットアップ

1. 資産生成
    * .\mvnw clean install
1. コンテナ構築
    * docker compose up -d --build

# Java資産の反映
    * docker restart my-tomcat
    
# SQL反映(Flyway起動)
    * docker compose run --rm flyway

# Playwriteのテスト実行
    * npx playwright test

# Flyway実行
    * cd .devcontainer
    * docker compose up flyway

# 修正後の資産適用

1. cd nwproject_docker
1. mvnw package
1. ブラウザ super reload

# VSCodeキャッシュ先
    * %APPDATA%\Code\User\workspaceStorage

# 技術スタック
 * Java(Servlet/JSP)
 * Docker
 * Tomcat
 * MySQL
 * Flyway
 * GitHub Actions(CI/CD)
 * Playwright(E2E)
 * Google Cloud Run