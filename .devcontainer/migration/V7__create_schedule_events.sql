CREATE TABLE schedule_event (
    event_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    start_at DATETIME NOT NULL,
    end_at DATETIME NOT NULL,
    all_day BOOLEAN NOT NULL DEFAULT FALSE,
    color VARCHAR(20) NOT NULL DEFAULT '#1a73e8',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_schedule_event_user FOREIGN KEY (user_id) REFERENCES users_info(user_id),
    CONSTRAINT chk_schedule_event_period CHECK (end_at > start_at),
    INDEX idx_schedule_event_user_period (user_id, start_at, end_at)
);
