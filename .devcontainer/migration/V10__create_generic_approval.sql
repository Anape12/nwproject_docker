ALTER TABLE attendance_record
ADD COLUMN approval_status VARCHAR(12) NOT NULL DEFAULT 'DRAFT' AFTER report_id,
ADD CONSTRAINT chk_attendance_approval_status
    CHECK (approval_status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED'));

CREATE TABLE approval_request (
    approval_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    applicant_id VARCHAR(20) NOT NULL,
    status VARCHAR(12) NOT NULL DEFAULT 'SUBMITTED',
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at DATETIME,
    reviewer_id VARCHAR(20),
    review_comment VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_approval_applicant FOREIGN KEY (applicant_id) REFERENCES users_info(user_id),
    CONSTRAINT fk_approval_reviewer FOREIGN KEY (reviewer_id) REFERENCES users_info(user_id),
    CONSTRAINT uq_approval_target UNIQUE (application_type, target_id),
    CONSTRAINT chk_approval_type CHECK (application_type IN ('REPORT', 'ATTENDANCE')),
    CONSTRAINT chk_approval_status CHECK (status IN ('SUBMITTED', 'APPROVED', 'REJECTED')),
    INDEX idx_approval_queue (status, submitted_at)
);

CREATE TABLE approval_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    approval_id BIGINT NOT NULL,
    action VARCHAR(12) NOT NULL,
    acted_by_id VARCHAR(20) NOT NULL,
    comment VARCHAR(1000),
    acted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_approval_history_request FOREIGN KEY (approval_id) REFERENCES approval_request(approval_id) ON DELETE CASCADE,
    CONSTRAINT fk_approval_history_user FOREIGN KEY (acted_by_id) REFERENCES users_info(user_id),
    CONSTRAINT chk_approval_action CHECK (action IN ('SUBMITTED', 'APPROVED', 'REJECTED')),
    INDEX idx_approval_history_request (approval_id, acted_at)
);
