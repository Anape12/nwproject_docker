CREATE TABLE audit_reason_mst (
    event_category VARCHAR(30) NOT NULL,
    reason_code VARCHAR(50) NOT NULL,
    message_template VARCHAR(500) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (event_category, reason_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO audit_reason_mst (event_category, reason_code, message_template) VALUES
('AUTH', 'NO_USER', '存在しないユーザーIDでログインを試行しました。'),
('AUTH', 'DISABLED', '無効化されたアカウントでログインを試行しました。'),
('AUTH', 'AI_ACCOUNT', 'AIアカウントでログインを試行しました。'),
('AUTH', 'ALREADY_LOCKED', 'ロック中のアカウントでログインを試行しました（解除予定: {lockUntil}）。'),
('AUTH', 'BAD_PASSWORD', 'パスワードが一致しません（失敗回数: {failedCount}）。'),
('AUTH', 'NOW_LOCKED', 'パスワード失敗が規定回数に達し、アカウントをロックしました（失敗回数: {failedCount}、解除予定: {lockUntil}）。'),
('AUTH', 'EXPIRED', 'パスワードの有効期限切れによりログインを拒否しました。'),
('AUTH', 'SUCCESS', 'ログインに成功しました。');

ALTER TABLE audit_log
    ADD COLUMN reason_code VARCHAR(50) NULL AFTER detail,
    ADD COLUMN detail_data JSON NULL AFTER reason_code,
    ADD INDEX idx_audit_reason (reason_code, created_at);
