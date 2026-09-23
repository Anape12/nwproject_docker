# 監査ログ・アカウントセキュリティ

## 実装範囲

- ログイン成功・失敗・ロック・無効アカウント・ログアウト・セッション終了
- ユーザー登録、氏名変更、権限変更
- 勤怠登録・修正・削除・打刻・月次締め
- 報告書／勤怠の申請・承認・差戻し・取下げ
- アカウント有効化・無効化、ロック解除、管理者によるパスワード再設定
- 本人によるパスワード変更
- 管理者向け監査ログ検索（最大500件）

監査ログにはパスワードやセッショントークンを保存しない。

## セキュリティ設定

`.env`で次の値を設定できる。

| 変数 | 既定値 | 内容 |
|---|---:|---|
| `SESSION_TIMEOUT_MINUTES` | `30` | 無操作セッションの有効時間（分） |
| `LOGIN_MAX_FAILURES` | `5` | アカウントを一時ロックする失敗回数 |
| `LOGIN_LOCK_MINUTES` | `15` | 一時ロック時間（分） |

## DB移行と再起動

アプリケーションより先にFlywayを実行する。

```bash
docker compose --env-file .env \
  -f .devcontainer/docker-compose.yml \
  -f .devcontainer/docker-compose.ai.yml \
  up -d db

docker compose --env-file .env \
  -f .devcontainer/docker-compose.yml \
  -f .devcontainer/docker-compose.ai.yml \
  run --rm flyway

docker compose --env-file .env \
  -f .devcontainer/docker-compose.yml \
  -f .devcontainer/docker-compose.ai.yml \
  up -d --build --force-recreate
```

管理者は業務メニューの「アカウント管理」と「監査ログ」から利用する。

