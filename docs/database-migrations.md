# DB マイグレーションの適用

Flyway は `.devcontainer/migration` 内の未適用 SQL を順番に実行します。通常のスキーマ更新では **DB ボリュームを削除しません**。以下は PowerShell・Linux シェルのどちらでも、`nwproject_docker` のプロジェクト直下から実行します。

1. 適用する SQL ファイルが実行環境にあることを確認します。Cloud では、先に変更をコミット・プッシュし、サーバー側に反映してください。例えば監査ログの `reason_code` 列は `V25__add_audit_reason_catalog.sql` に含まれます。
2. DB を起動し、適用状況を確認します。

```bash
docker compose --env-file .env -f .devcontainer/docker-compose.yml up -d db
docker compose --env-file .env -f .devcontainer/docker-compose.yml run --rm flyway info
```

3. 未適用のマイグレーションを実行し、再度状態を確認します。

```bash
docker compose --env-file .env -f .devcontainer/docker-compose.yml run --rm flyway
docker compose --env-file .env -f .devcontainer/docker-compose.yml run --rm flyway info
```

`info` に目的のバージョン（例: `25`）が適用済みとして表示されることを確認してください。DB が更新された後は、既に起動している Tomcat で再ログイン・再操作できます。コードも変更した場合は、マイグレーションを先に適用してから新しいアプリを起動します。

Cloud の通常デプロイでは `scripts/deploy.sh` が DB 起動、Flyway 実行、Tomcat 再作成の順に処理します。ただし、マイグレーションファイルが Cloud 側のチェックアウトに存在しなければ適用できません。

Flyway が失敗した場合は、そのエラーと `info` の結果を確認してください。適用済み SQL の編集、`repair`、`docker compose down -v` は原因を確認せずに実行しないでください。`down -v` は DB データを削除します。
