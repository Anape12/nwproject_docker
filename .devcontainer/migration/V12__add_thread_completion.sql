ALTER TABLE thread_info
    ADD COLUMN status VARCHAR(12) NOT NULL DEFAULT 'OPEN' AFTER thread_content,
    ADD COLUMN closed_by_id VARCHAR(20) NULL AFTER status,
    ADD COLUMN closed_at DATETIME NULL AFTER closed_by_id,
    ADD INDEX idx_thread_status_updated (status, updated_at);
