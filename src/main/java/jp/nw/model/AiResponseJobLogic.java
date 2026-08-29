package jp.nw.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jp.nw.parts.DBBase;

public final class AiResponseJobLogic {
    private AiResponseJobLogic() {
    }

    public static void enqueueChat(Connection c, long messageId, String roomId, String requester, String message)
            throws SQLException {
        String sql = "INSERT IGNORE INTO ai_response_job(character_id,source_type,conversation_id,source_message_id,requested_by_id) "
                + "SELECT a.character_id,'CHAT',?, ?, ? FROM ai_character a JOIN users_info u ON u.user_id=a.user_id "
                + "JOIN chat_room_member m ON m.user_id=a.user_id JOIN chat_room r ON r.room_id=m.room_id "
                + "WHERE m.room_id=? AND a.active_flg='1' AND u.delete_flg='0' AND "
                + "(r.room_type='1' OR a.reply_mode='ALWAYS' OR LOCATE(CONCAT('@',a.character_name),?)>0 OR LOCATE(CONCAT('@',a.user_id),?)>0)";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, roomId);
            p.setLong(2, messageId);
            p.setString(3, requester);
            p.setString(4, roomId);
            p.setString(5, message);
            p.setString(6, message);
            p.executeUpdate();
        }
    }

    public static void enqueueThread(Connection c, long commentId, int threadId, String requester, String message)
            throws SQLException {
        String sql = "INSERT IGNORE INTO ai_response_job(character_id,source_type,conversation_id,source_message_id,requested_by_id) "
                + "SELECT a.character_id,'THREAD',?, ?, ? FROM ai_character a JOIN users_info u ON u.user_id=a.user_id "
                + "WHERE a.active_flg='1' AND u.delete_flg='0' AND (LOCATE(CONCAT('@',a.character_name),?)>0 OR LOCATE(CONCAT('@',a.user_id),?)>0)";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, String.valueOf(threadId));
            p.setLong(2, commentId);
            p.setString(3, requester);
            p.setString(4, message);
            p.setString(5, message);
            p.executeUpdate();
        }
    }

    public static boolean hasPending(String type, String conversationId) {
        DBBase db = new DBBase();
        try (Connection c = db.getConnection();
                PreparedStatement p = c.prepareStatement("SELECT 1 FROM ai_response_job WHERE source_type=? AND conversation_id=? AND status IN ('PENDING','PROCESSING') LIMIT 1")) {
            p.setString(1, type);
            p.setString(2, conversationId);
            try (ResultSet r = p.executeQuery()) { return r.next(); }
        } catch (SQLException e) { return false; }
    }
}
