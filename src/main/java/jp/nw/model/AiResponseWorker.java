package jp.nw.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jp.nw.parts.DBBase;

public class AiResponseWorker implements Runnable {
    private final AiServiceClient client = new AiServiceClient();

    public void run() {
        try {
            Job j = claim();
            if (j != null)
                process(j);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Job claim() throws SQLException {
        DBBase db = new DBBase();
        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement(
                    "SELECT j.job_id,j.character_id,j.source_type,j.conversation_id,j.source_message_id,j.requested_by_id,a.user_id,a.character_name,a.system_prompt,a.personality,a.interests,a.model_name FROM ai_response_job j JOIN ai_character a ON a.character_id=j.character_id WHERE j.status='PENDING' AND a.active_flg='1' ORDER BY j.created_at LIMIT 1 FOR UPDATE")) {
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) {
                        c.commit();
                        return null;
                    }
                    Job j = new Job(r.getLong(1), r.getLong(2), r.getString(3), r.getString(4), r.getLong(5),
                            r.getString(6), r.getString(7), r.getString(8), r.getString(9), r.getString(10),
                            r.getString(11), r.getString(12));
                    try (PreparedStatement u = c.prepareStatement(
                            "UPDATE ai_response_job SET status='PROCESSING',started_at=NOW() WHERE job_id=?")) {
                        u.setLong(1, j.id);
                        u.executeUpdate();
                    }
                    c.commit();
                    return j;
                }
            }
        }
    }

    private void process(Job j) {
        try {
            Source s = source(j);
            String answer = client.respond(j.characterId, j.name, j.prompt, j.personality, j.interests, j.model, j.type,
                    j.conversationId, s.context, s.message);
            complete(j, answer);
        } catch (Exception e) {
            fail(j, e);
        }
    }

    private Source source(Job j) throws SQLException {
        DBBase db = new DBBase();
        try (Connection c = db.getConnection()) {
            String messageSql = "CHAT".equals(j.type) ? "SELECT message FROM chat_message WHERE message_id=?"
                    : "SELECT comment_text FROM thread_comment WHERE comment_id=?";
            String message;
            try (PreparedStatement p = c.prepareStatement(messageSql)) {
                p.setLong(1, j.sourceMessageId);
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next())
                        throw new SQLException("Source message not found");
                    message = r.getString(1);
                }
            }
            String contextSql = "CHAT".equals(j.type)
                    ? "SELECT CONCAT(COALESCE(CONCAT(u.last_name,' ',u.first_name),m.posted_by_id),': ',m.message) line FROM chat_message m LEFT JOIN users_info u ON u.user_id=m.posted_by_id WHERE m.room_id=? AND m.message_id<? AND m.delete_flg='0' ORDER BY m.message_id DESC LIMIT 12"
                    : "SELECT line FROM (SELECT 0 sort_no,CONCAT('スレッド題名: ',title,'\n本文: ',thread_content) line FROM thread_info WHERE thread_id=? UNION ALL SELECT comment_id sort_no,CONCAT(author_id,': ',comment_text) line FROM thread_comment WHERE thread_id=? AND comment_id<?) x ORDER BY sort_no DESC LIMIT 12";
            StringBuilder context = new StringBuilder();
            try (PreparedStatement p = c.prepareStatement(contextSql)) {
                p.setString(1, j.conversationId);
                if ("CHAT".equals(j.type)) {
                    p.setLong(2, j.sourceMessageId);
                } else {
                    p.setString(2, j.conversationId);
                    p.setLong(3, j.sourceMessageId);
                }
                try (ResultSet r = p.executeQuery()) {
                    while (r.next())
                        context.insert(0, r.getString("line") + "\n");
                }
            }
            return new Source(message, context.toString());
        }
    }

    private void complete(Job j, String answer) throws SQLException {
        DBBase db = new DBBase();
        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try {
                if ("CHAT".equals(j.type)) {
                    try (PreparedStatement p = c
                            .prepareStatement("INSERT INTO chat_message(room_id,posted_by_id,message) VALUES(?,?,?)")) {
                        p.setString(1, j.conversationId);
                        p.setString(2, j.aiUserId);
                        p.setString(3, answer);
                        p.executeUpdate();
                    }
                    try (PreparedStatement p = c
                            .prepareStatement("UPDATE chat_room SET updated_at=NOW() WHERE room_id=?")) {
                        p.setString(1, j.conversationId);
                        p.executeUpdate();
                    }
                } else {
                    try (PreparedStatement p = c.prepareStatement(
                            "INSERT INTO thread_comment(thread_id,author_id,comment_text) VALUES(?,?,?)")) {
                        p.setInt(1, Integer.parseInt(j.conversationId));
                        p.setString(2, j.aiUserId);
                        p.setString(3, answer);
                        p.executeUpdate();
                    }
                }
                try (PreparedStatement p = c.prepareStatement(
                        "INSERT INTO ai_conversation_memory(character_id,source_type,conversation_id,memory_summary) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE memory_summary=VALUES(memory_summary)")) {
                    p.setLong(1, j.characterId);
                    p.setString(2, j.type);
                    p.setString(3, j.conversationId);
                    p.setString(4, answer);
                    p.executeUpdate();
                }
                try (PreparedStatement p = c.prepareStatement(
                        "UPDATE ai_response_job SET status='COMPLETED',completed_at=NOW(),error_message=NULL WHERE job_id=?")) {
                    p.setLong(1, j.id);
                    p.executeUpdate();
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    private void fail(Job j, Exception e) {
        DBBase db = new DBBase();
        try (Connection c = db.getConnection();
                PreparedStatement p = c.prepareStatement(
                        "UPDATE ai_response_job SET retry_count=retry_count+1,status=IF(retry_count+1>=3,'FAILED','PENDING'),error_message=? WHERE job_id=?")) {
            String m = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            p.setString(1, m.length() > 500 ? m.substring(0, 500) : m);
            p.setLong(2, j.id);
            p.executeUpdate();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    private record Job(long id, long characterId, String type, String conversationId, long sourceMessageId,
            String requester, String aiUserId, String name, String prompt, String personality, String interests,
            String model) {
    }

    private record Source(String message, String context) {
    }
}
