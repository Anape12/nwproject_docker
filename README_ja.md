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

# Prettier の適用

-   npm run format:jsp

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

# Qiita Guide（記事の読み上げ）との連携

業務メニューの「Qiita記事を聴く」から、別プロジェクトの`AI_Support`（Qiita Guide）を新しいタブで開きます。NW ProjectとQiita Guideはそれぞれ起動する必要があります。

ローカルでは`AI_Support`を`nwproject_docker`と同階層に配置し、`AI_Support/.env`にOpenAI APIキーを設定してください。Qiita Guide用のComposeファイルを追加すれば、Node.jsを別ターミナルで起動する必要はありません。PowerShellで`nwproject_docker`直下から実行します。

```powershell
docker compose --env-file .env -f .devcontainer/docker-compose.yml -f .devcontainer/docker-compose.ai.yml -f .devcontainer/docker-compose.qiita.yml up -d --build
```

Qiita Guideだけを再ビルドして起動する場合は次のコマンドを使います。

```powershell
docker compose --env-file .env -f .devcontainer/docker-compose.yml -f .devcontainer/docker-compose.ai.yml -f .devcontainer/docker-compose.qiita.yml up -d --build qiita-guide
```

`http://localhost:3000/api/status`で起動状態を確認できます。NW Projectを`localhost`で開いている場合、`QIITA_GUIDE_URL`を省略すると`http://localhost:3000/`を使用します。Qiita Guideのポートはホストのループバックアドレスにだけ公開します。

別端末やCloudで利用する場合は、Qiita Guideをアクセス可能なHTTPSのURLで公開し、NW Projectの`.env`に接続先を設定します。

```text
QIITA_GUIDE_URL=https://reader.example.com/
```

設定変更後はTomcatコンテナを再作成して環境変数を反映します。Cloudでは`localhost`を接続先に指定しないでください。上記Compose設定はローカル用で、Cloudでブラウザーから使うにはHTTPSのリバースプロキシとQiita Guide側のアクセス制御が別途必要です。NW Projectのログイン制御はメニューの起動口にのみ適用されます。

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
