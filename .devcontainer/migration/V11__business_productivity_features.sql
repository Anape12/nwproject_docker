-- 通知
CREATE TABLE notification (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL,
    category VARCHAR(30) NOT NULL,
    title VARCHAR(150) NOT NULL,
    message VARCHAR(500),
    link_url VARCHAR(500),
    read_at DATETIME,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users_info(user_id) ON DELETE CASCADE,
    INDEX idx_notification_user (user_id, read_at, created_at)
);

-- 勤怠の実務項目と月次締め
ALTER TABLE attendance_record
    ADD COLUMN attendance_type VARCHAR(20) NOT NULL DEFAULT 'NORMAL' AFTER work_type,
    ADD COLUMN overtime_minutes INT NOT NULL DEFAULT 0 AFTER break_minutes,
    ADD COLUMN corrected_by_id VARCHAR(20) AFTER approval_status,
    ADD COLUMN correction_reason VARCHAR(500) AFTER corrected_by_id,
    ADD CONSTRAINT fk_attendance_corrector FOREIGN KEY (corrected_by_id) REFERENCES users_info(user_id),
    ADD CONSTRAINT chk_attendance_detail_type CHECK (attendance_type IN ('NORMAL','LATE','EARLY','ABSENT','PAID_LEAVE','COMP_LEAVE','HOLIDAY_WORK'));

CREATE TABLE attendance_month_close (
    close_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL,
    target_month DATE NOT NULL,
    closed_by_id VARCHAR(20) NOT NULL,
    closed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_month_close_user FOREIGN KEY (user_id) REFERENCES users_info(user_id),
    CONSTRAINT fk_month_close_admin FOREIGN KEY (closed_by_id) REFERENCES users_info(user_id),
    CONSTRAINT uq_month_close UNIQUE (user_id, target_month)
);

CREATE TABLE attendance_change_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attendance_id BIGINT NOT NULL,
    changed_by_id VARCHAR(20) NOT NULL,
    change_summary VARCHAR(1000) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_change_target FOREIGN KEY (attendance_id) REFERENCES attendance_record(attendance_id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_change_user FOREIGN KEY (changed_by_id) REFERENCES users_info(user_id)
);

-- 承認の取下げ・段階・一括処理
ALTER TABLE approval_request
    MODIFY status VARCHAR(12) NOT NULL DEFAULT 'SUBMITTED',
    ADD COLUMN approval_step INT NOT NULL DEFAULT 1 AFTER status,
    ADD COLUMN required_steps INT NOT NULL DEFAULT 1 AFTER approval_step,
    ADD COLUMN withdrawn_at DATETIME AFTER reviewed_at,
    DROP CHECK chk_approval_status,
    ADD CONSTRAINT chk_approval_status_v2 CHECK (status IN ('SUBMITTED','APPROVED','REJECTED','WITHDRAWN'));

ALTER TABLE approval_history
    DROP CHECK chk_approval_action,
    ADD CONSTRAINT chk_approval_action_v2 CHECK (action IN ('SUBMITTED','APPROVED','REJECTED','WITHDRAWN','COMMENTED'));

CREATE TABLE approval_delegate (
    delegate_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    approver_id VARCHAR(20) NOT NULL,
    delegate_user_id VARCHAR(20) NOT NULL,
    valid_from DATE NOT NULL,
    valid_to DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_delegate_approver FOREIGN KEY (approver_id) REFERENCES users_info(user_id),
    CONSTRAINT fk_delegate_user FOREIGN KEY (delegate_user_id) REFERENCES users_info(user_id),
    CONSTRAINT uq_delegate_period UNIQUE (approver_id, delegate_user_id, valid_from)
);

CREATE TABLE approval_route_config (
    application_type VARCHAR(20) PRIMARY KEY,
    required_steps INT NOT NULL DEFAULT 1,
    CONSTRAINT chk_route_steps CHECK (required_steps BETWEEN 1 AND 5)
);
INSERT INTO approval_route_config(application_type,required_steps) VALUES ('REPORT',1),('ATTENDANCE',1);

-- 全機能共通の添付ファイル（実体はDocker volume上へ保存）
CREATE TABLE attachment (
    attachment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_type VARCHAR(30) NOT NULL,
    owner_id VARCHAR(64) NOT NULL,
    uploaded_by_id VARCHAR(20) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(100) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attachment_user FOREIGN KEY (uploaded_by_id) REFERENCES users_info(user_id),
    INDEX idx_attachment_owner (owner_type, owner_id)
);

-- 共有予定、参加回答、繰返し
ALTER TABLE schedule_event
    ADD COLUMN visibility VARCHAR(12) NOT NULL DEFAULT 'PRIVATE' AFTER color,
    ADD COLUMN recurrence_rule VARCHAR(100) AFTER visibility,
    ADD COLUMN recurrence_until DATE AFTER recurrence_rule,
    ADD CONSTRAINT chk_schedule_visibility CHECK (visibility IN ('PRIVATE','SHARED'));

CREATE TABLE schedule_participant (
    event_id BIGINT NOT NULL,
    user_id VARCHAR(20) NOT NULL,
    response_status VARCHAR(12) NOT NULL DEFAULT 'PENDING',
    responded_at DATETIME,
    PRIMARY KEY (event_id, user_id),
    CONSTRAINT fk_schedule_participant_event FOREIGN KEY (event_id) REFERENCES schedule_event(event_id) ON DELETE CASCADE,
    CONSTRAINT fk_schedule_participant_user FOREIGN KEY (user_id) REFERENCES users_info(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_schedule_response CHECK (response_status IN ('PENDING','ACCEPTED','DECLINED'))
);
