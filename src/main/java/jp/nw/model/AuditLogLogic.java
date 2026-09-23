package jp.nw.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import jp.nw.entity.AuditLogEntity;
import jp.nw.entity.UserEntity;
import jp.nw.parts.DBBase;

public final class AuditLogLogic {
    private AuditLogLogic() {
    }

    public static void record(HttpServletRequest request, String category, String action,
            String targetType, String targetId, boolean success, String detail) {
        UserEntity actor = null;
        if (request != null && request.getSession(false) != null) {
            actor = (UserEntity) request.getSession(false).getAttribute("loginUser");
        }
        record(actor == null ? null : actor.getUserId(), category, action, targetType, targetId,
                success, clientIp(request), request == null ? null : request.getHeader("User-Agent"), detail);
    }

    public static void record(String actorId, String category, String action, String targetType,
            String targetId, boolean success, String ip, String userAgent, String detail) {
        DBBase db = new DBBase();
        try (Connection con = db.getConnection()) {
            record(con, actorId, category, action, targetType, targetId, success, ip, userAgent, detail);
        } catch (SQLException e) {
            // 監査記録の失敗で本来の業務処理を壊さない。サーバーログには必ず残す。
            System.err.println("Failed to write audit log: " + e.getMessage());
        }
    }

    public static void record(Connection con, String actorId, String category, String action,
            String targetType, String targetId, boolean success, String ip, String userAgent,
            String detail) throws SQLException {
        String sql = "INSERT INTO audit_log(event_category,event_action,actor_user_id,target_type,target_id,success,ip_address,user_agent,detail) VALUES(?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, limit(category, 30));
            ps.setString(2, limit(action, 50));
            ps.setString(3, limit(actorId, 20));
            ps.setString(4, limit(targetType, 30));
            ps.setString(5, limit(targetId, 64));
            ps.setBoolean(6, success);
            ps.setString(7, limit(ip, 45));
            ps.setString(8, limit(userAgent, 500));
            ps.setString(9, limit(detail, 2000));
            ps.executeUpdate();
        }
    }

    public static List<AuditLogEntity> search(String userId, String category, String action,
            Boolean success, LocalDate from, LocalDate to, int limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_log WHERE 1=1");
        List<Object> values = new ArrayList<>();
        if (userId != null && !userId.isBlank()) {
            sql.append(" AND (actor_user_id=? OR target_id=?)");
            values.add(userId);
            values.add(userId);
        }
        if (category != null && !category.isBlank()) {
            sql.append(" AND event_category=?");
            values.add(category);
        }
        if (action != null && !action.isBlank()) {
            sql.append(" AND event_action LIKE ?");
            values.add("%" + action + "%");
        }
        if (success != null) {
            sql.append(" AND success=?");
            values.add(success);
        }
        if (from != null) {
            sql.append(" AND created_at>=?");
            values.add(from.atStartOfDay());
        }
        if (to != null) {
            sql.append(" AND created_at<?");
            values.add(to.plusDays(1).atStartOfDay());
        }
        sql.append(" ORDER BY created_at DESC,audit_id DESC LIMIT ?");
        values.add(Math.min(Math.max(limit, 1), 500));
        DBBase db = new DBBase();
        try (Connection con = db.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < values.size(); i++)
                ps.setObject(i + 1, values.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                List<AuditLogEntity> result = new ArrayList<>();
                while (rs.next())
                    result.add(map(rs));
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("監査ログの検索に失敗しました。", e);
        }
    }

    private static AuditLogEntity map(ResultSet rs) throws SQLException {
        Timestamp created = rs.getTimestamp("created_at");
        return AuditLogEntity.builder().auditId(rs.getLong("audit_id"))
                .eventCategory(rs.getString("event_category")).eventAction(rs.getString("event_action"))
                .actorUserId(rs.getString("actor_user_id")).targetType(rs.getString("target_type"))
                .targetId(rs.getString("target_id")).success(rs.getBoolean("success"))
                .ipAddress(rs.getString("ip_address")).userAgent(rs.getString("user_agent"))
                .detail(rs.getString("detail")).createdAt(created.toLocalDateTime()).build();
    }

    public static String clientIp(HttpServletRequest request) {
        if (request == null)
            return null;
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank() ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
    }

    private static String limit(String value, int max) {
        if (value == null)
            return null;
        return value.length() <= max ? value : value.substring(0, max);
    }
}
