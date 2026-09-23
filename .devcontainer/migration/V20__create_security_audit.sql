ALTER TABLE users_info
    ADD COLUMN account_disabled BOOLEAN NOT NULL DEFAULT FALSE AFTER delete_flg,
    ADD COLUMN failed_login_count INT NOT NULL DEFAULT 0 AFTER account_disabled,
    ADD COLUMN locked_until DATETIME AFTER failed_login_count,
    ADD COLUMN last_login_at DATETIME AFTER locked_until,
    ADD COLUMN password_changed_at DATETIME AFTER last_login_at,
    ADD COLUMN force_password_change BOOLEAN NOT NULL DEFAULT FALSE AFTER password_changed_at;

UPDATE users_info
SET account_disabled = (delete_flg <> '0'),
    password_changed_at = NOW();

CREATE TABLE audit_log (
    audit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_category VARCHAR(30) NOT NULL,
    event_action VARCHAR(50) NOT NULL,
    actor_user_id VARCHAR(20),
    target_type VARCHAR(30),
    target_id VARCHAR(64),
    success BOOLEAN NOT NULL DEFAULT TRUE,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    detail VARCHAR(2000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_created (created_at),
    INDEX idx_audit_actor (actor_user_id, created_at),
    INDEX idx_audit_target (target_type, target_id, created_at),
    INDEX idx_audit_event (event_category, event_action, created_at)
);

