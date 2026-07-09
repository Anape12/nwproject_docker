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
    * npx playwrite test

# 修正後の資産適用

1. cd nwproject_docker
1. mvnw package
1. ブラウザ super reload

# VSCodeキャッシュ先
    * %APPDATA%\Code\User\workspaceStorage