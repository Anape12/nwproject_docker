CREATE TABLE work_report (
    report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id VARCHAR(20) NOT NULL,
    report_date DATE NOT NULL,
    title VARCHAR(150) NOT NULL,
    body TEXT NOT NULL,
    status VARCHAR(12) NOT NULL DEFAULT 'DRAFT',
    submitted_at DATETIME,
    reviewed_at DATETIME,
    reviewed_by_id VARCHAR(20),
    review_comment VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_work_report_author FOREIGN KEY (author_id) REFERENCES users_info(user_id),
    CONSTRAINT fk_work_report_reviewer FOREIGN KEY (reviewed_by_id) REFERENCES users_info(user_id),
    CONSTRAINT chk_work_report_status CHECK (status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED')),
    INDEX idx_work_report_author (author_id, report_date),
    INDEX idx_work_report_status (status, submitted_at)
);

CREATE TABLE work_report_review (
    review_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id BIGINT NOT NULL,
    reviewer_id VARCHAR(20) NOT NULL,
    decision VARCHAR(10) NOT NULL,
    comment VARCHAR(1000),
    reviewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_work_report_review_report FOREIGN KEY (report_id) REFERENCES work_report(report_id) ON DELETE CASCADE,
    CONSTRAINT fk_work_report_review_user FOREIGN KEY (reviewer_id) REFERENCES users_info(user_id),
    CONSTRAINT chk_work_report_decision CHECK (decision IN ('APPROVED', 'REJECTED')),
    INDEX idx_work_report_review_report (report_id, reviewed_at)
);
