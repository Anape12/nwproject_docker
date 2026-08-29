ALTER TABLE users_info
    ADD COLUMN account_type VARCHAR(10) NOT NULL DEFAULT 'HUMAN' AFTER permission,
    ADD INDEX idx_users_account_type (account_type, delete_flg);

CREATE TABLE ai_character (
    character_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL UNIQUE,
    character_name VARCHAR(50) NOT NULL,
    system_prompt TEXT NOT NULL,
    personality TEXT,
    interests TEXT,
    model_name VARCHAR(100),
    reply_mode VARCHAR(12) NOT NULL DEFAULT 'MENTION',
    active_flg CHAR(1) NOT NULL DEFAULT '1',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_character_user FOREIGN KEY (user_id) REFERENCES users_info(user_id),
    INDEX idx_ai_character_active (active_flg, reply_mode)
);

CREATE TABLE ai_response_job (
    job_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL,
    source_type VARCHAR(10) NOT NULL,
    conversation_id VARCHAR(64) NOT NULL,
    source_message_id BIGINT NOT NULL,
    requested_by_id VARCHAR(20) NOT NULL,
    status VARCHAR(12) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    error_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    CONSTRAINT fk_ai_job_character FOREIGN KEY (character_id) REFERENCES ai_character(character_id),
    CONSTRAINT fk_ai_job_requester FOREIGN KEY (requested_by_id) REFERENCES users_info(user_id),
    CONSTRAINT uk_ai_job_source UNIQUE (character_id, source_type, source_message_id),
    INDEX idx_ai_job_queue (status, created_at)
);

CREATE TABLE ai_conversation_memory (
    character_id BIGINT NOT NULL,
    source_type VARCHAR(10) NOT NULL,
    conversation_id VARCHAR(64) NOT NULL,
    memory_summary TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (character_id, source_type, conversation_id),
    CONSTRAINT fk_ai_memory_character FOREIGN KEY (character_id) REFERENCES ai_character(character_id)
);

INSERT INTO users_info
    (user_id, password, birthday, permission, account_type, password_expiration, delete_flg, first_name, last_name)
VALUES
    ('ai_mina', '$2a$10$45HSUdWr4xrIYVymHlDmL.v0sc6xpENpHAszdaiSUG8bVWKuUs5LK', '2000-01-01', '2', 'AI', '00000000', '0', 'ミナ', 'AI住人');

INSERT INTO ai_character
    (user_id, character_name, system_prompt, personality, interests, reply_mode)
VALUES
    ('ai_mina', 'ミナ',
     'あなたは社内コミュニケーションシステムに暮らすAI住人のミナです。AIであることを隠さず、会話の流れを読み、短く自然な日本語で返答してください。知らないことを体験したふりはしないでください。',
     '親しみやすく、少しユーモアがある。押しつけがましくせず、相手の話を広げる。',
     'ガジェット、仕事術、雑談、技術', 'MENTION');
