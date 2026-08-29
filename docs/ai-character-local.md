# AI住人 ローカル起動手順

## 前提

- `nwproject_docker` と `My-AiCreate` を同じ `workspace` ディレクトリ直下に配置する。
- Ollamaを起動し、使用するモデル（既定値は `qwen2.5:3b`）を取得しておく。

```powershell
ollama pull qwen2.5:3b
```

## 起動

`.devcontainer` ディレクトリで、AI用のCompose定義を重ねて起動する。

```bash
docker compose -f docker-compose.yml -f docker-compose.ai.yml up -d db ai-service
docker compose -f docker-compose.yml -f docker-compose.ai.yml run --rm flyway
docker compose -f docker-compose.yml -f docker-compose.ai.yml run --rm -w /workspaces app mvn clean package
docker compose -f docker-compose.yml -f docker-compose.ai.yml up -d --build
```

管理者でログインし、業務メニューの「AI住人管理」から人格を設定する。初期データとしてAI住人「ミア」が登録される（内部ユーザーIDは既存互換のため `ai_mina`）。

## 応答条件

- AIとの個別チャット: すべての投稿へ応答する。
- グループチャット: 応答方法が「常時」、または `@ミア` / `@ai_mina` が含まれる場合に応答する。
- スレッド: `@ミア` / `@ai_mina` が含まれる場合に応答する。
- AI回答は非同期で生成され、チャットは3秒間隔、スレッドは回答待ちの間だけ自動更新される。

## 設定値

環境変数で変更できる。

| 変数 | 既定値 | 用途 |
|---|---|---|
| `LLM_PROVIDER` | `ollama` | `ollama` または `external` |
| `OLLAMA_MODEL` | `qwen2.5:3b` | Ollamaモデル名 |
| `AI_ONLINE` | `false` | オンラインツールの有効化 |
| `AI_SERVICE_TOKEN` | `local-ai-token` | NWProjectとAIサービス間の内部認証 |

AI応答が3回失敗すると `ai_response_job.status` は `FAILED` になる。原因は同テーブルの `error_message` で確認できる。
