CREATE TABLE error_message_mst (
    error_code VARCHAR(32) PRIMARY KEY,
    message_text VARCHAR(500) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO error_message_mst (error_code, message_text) VALUES
('AUTH-001', 'ユーザーIDまたはパスワードが正しくありません。'),
('AUTH-002', 'このアカウントは利用できません。管理者へお問い合わせください。'),
('AUTH-003', 'このアカウントではログインできません。'),
('AUTH-004', 'ログイン失敗が規定回数を超えたため、一時的にロックされています。'),
('AUTH-005', 'ログイン失敗が規定回数を超えたため、一時的にロックされました。'),
('AUTH-006', 'パスワードの有効期限が切れています。管理者へ再設定を依頼してください。');
