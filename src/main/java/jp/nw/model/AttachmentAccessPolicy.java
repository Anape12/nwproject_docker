package jp.nw.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jp.nw.entity.UserEntity;
import jp.nw.parts.DBBase;

public class AttachmentAccessPolicy {
    public boolean canWrite(UserEntity user, AttachmentOwnerType type, String ownerId) {
        if (!validUser(user)) return false;
        return switch (type) {
            case REPORT -> exists(
                    "SELECT 1 FROM work_report WHERE report_id=? AND author_id=? AND status IN ('DRAFT','REJECTED')",
                    ownerId, user.getUserId());
            case CHAT -> exists(
                    "SELECT 1 FROM chat_room r INNER JOIN chat_room_member m ON m.room_id=r.room_id "
                            + "WHERE r.room_id=? AND m.user_id=? AND r.delete_flg='0'",
                    ownerId, user.getUserId());
            case SCHEDULE -> exists("SELECT 1 FROM schedule_event WHERE event_id=? AND user_id=?",
                    ownerId, user.getUserId());
            case APPROVAL -> exists(
                    "SELECT 1 FROM approval_request WHERE approval_id=? AND applicant_id=? AND status='SUBMITTED'",
                    ownerId, user.getUserId());
        };
    }

    public boolean canRead(UserEntity user, AttachmentOwnerType type, String ownerId) {
        if (!validUser(user)) return false;
        return switch (type) {
            case REPORT -> isReviewer(user)
                    || exists("SELECT 1 FROM work_report WHERE report_id=? AND author_id=?",
                            ownerId, user.getUserId());
            case CHAT -> exists(
                    "SELECT 1 FROM chat_room r INNER JOIN chat_room_member m ON m.room_id=r.room_id "
                            + "WHERE r.room_id=? AND m.user_id=? AND r.delete_flg='0'",
                    ownerId, user.getUserId());
            case SCHEDULE -> exists(
                    "SELECT 1 FROM schedule_event e LEFT JOIN schedule_participant p ON p.event_id=e.event_id "
                            + "WHERE e.event_id=? AND (e.user_id=? OR e.visibility='SHARED' OR p.user_id=?)",
                    ownerId, user.getUserId(), user.getUserId());
            case APPROVAL -> isReviewer(user)
                    || exists("SELECT 1 FROM approval_request WHERE approval_id=? AND applicant_id=?",
                            ownerId, user.getUserId());
        };
    }

    private boolean isReviewer(UserEntity user) {
        return "1".equals(user.getPermission())
                || new ApprovalLogic().canReview(user.getUserId(), user.getPermission());
    }

    private boolean validUser(UserEntity user) {
        return user != null && user.getUserId() != null && !user.getUserId().isBlank();
    }

    private boolean exists(String sql, Object... parameters) {
        DBBase db = new DBBase();
        try (Connection connection = db.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) statement.setObject(i + 1, parameters[i]);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("添付ファイルの権限確認に失敗しました。", e);
        }
    }
}
