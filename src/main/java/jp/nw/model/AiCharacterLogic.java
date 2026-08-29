package jp.nw.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import jp.nw.entity.AiCharacterEntity;
import jp.nw.parts.DBBase;

public class AiCharacterLogic {
    public List<AiCharacterEntity> findAll() {
        String sql = "SELECT character_id,user_id,character_name,system_prompt,personality,interests,model_name,reply_mode,active_flg FROM ai_character ORDER BY character_id";
        List<AiCharacterEntity> result = new ArrayList<>();
        DBBase db = new DBBase();
        try (Connection c = db.getConnection();
                PreparedStatement p = c.prepareStatement(sql);
                ResultSet r = p.executeQuery()) {
            while (r.next())
                result.add(map(r));
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("AI住人の取得に失敗しました。", e);
        }
    }

    public AiCharacterEntity find(long id) {
        DBBase db = new DBBase();
        try (Connection c = db.getConnection();
                PreparedStatement p = c.prepareStatement(
                        "SELECT character_id,user_id,character_name,system_prompt,personality,interests,model_name,reply_mode,active_flg FROM ai_character WHERE character_id=?")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("AI住人の取得に失敗しました。", e);
        }
    }

    public List<Map<String, Object>> findRecentJobs() {
        String sql = "SELECT j.job_id,a.character_name,j.source_type,j.status,j.retry_count,j.error_message,j.created_at,j.completed_at FROM ai_response_job j JOIN ai_character a ON a.character_id=j.character_id ORDER BY j.job_id DESC LIMIT 20";
        List<Map<String, Object>> list = new ArrayList<>();
        DBBase db = new DBBase();
        try (Connection c = db.getConnection(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
            while (r.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("jobId", r.getLong("job_id")); row.put("characterName", r.getString("character_name"));
                row.put("sourceType", r.getString("source_type")); row.put("status", r.getString("status"));
                row.put("retryCount", r.getInt("retry_count")); row.put("errorMessage", r.getString("error_message"));
                row.put("createdAt", r.getTimestamp("created_at")); row.put("completedAt", r.getTimestamp("completed_at"));
                list.add(row);
            }
        } catch (SQLException ignored) { }
        return list;
    }

    public void create(AiCharacterEntity v) {
        validate(v);
        DBBase db = new DBBase();
        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement p = c.prepareStatement(
                        "INSERT INTO users_info(user_id,password,birthday,permission,account_type,password_expiration,delete_flg,first_name,last_name) VALUES(?,'$2a$10$45HSUdWr4xrIYVymHlDmL.v0sc6xpENpHAszdaiSUG8bVWKuUs5LK','2000-01-01','2','AI','00000000','0',?,'AI住人')")) {
                    p.setString(1, v.getUserId());
                    p.setString(2, v.getCharacterName());
                    p.executeUpdate();
                }
                try (PreparedStatement p = c.prepareStatement(
                        "INSERT INTO ai_character(user_id,character_name,system_prompt,personality,interests,model_name,reply_mode,active_flg) VALUES(?,?,?,?,?,?,?,'1')")) {
                    bind(p, v, false);
                    p.executeUpdate();
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("AI住人の登録に失敗しました。", e);
        }
    }

    public void update(AiCharacterEntity v) {
        validate(v);
        DBBase db = new DBBase();
        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement p = c.prepareStatement(
                        "UPDATE ai_character SET character_name=?,system_prompt=?,personality=?,interests=?,model_name=?,reply_mode=?,active_flg=? WHERE character_id=?")) {
                    p.setString(1, v.getCharacterName());
                    p.setString(2, v.getSystemPrompt());
                    p.setString(3, v.getPersonality());
                    p.setString(4, v.getInterests());
                    p.setString(5, blankToNull(v.getModelName()));
                    p.setString(6, v.getReplyMode());
                    p.setString(7, v.getActiveFlg());
                    p.setLong(8, v.getCharacterId());
                    if (p.executeUpdate() != 1)
                        throw new IllegalArgumentException("AI住人が見つかりません。");
                }
                try (PreparedStatement p = c.prepareStatement(
                        "UPDATE users_info u JOIN ai_character a ON a.user_id=u.user_id SET u.first_name=?,u.delete_flg=IF(?='1','0','1') WHERE a.character_id=?")) {
                    p.setString(1, v.getCharacterName());
                    p.setString(2, v.getActiveFlg());
                    p.setLong(3, v.getCharacterId());
                    p.executeUpdate();
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("AI住人の更新に失敗しました。", e);
        }
    }

    private void bind(PreparedStatement p, AiCharacterEntity v, boolean ignored) throws SQLException {
        p.setString(1, v.getUserId());
        p.setString(2, v.getCharacterName());
        p.setString(3, v.getSystemPrompt());
        p.setString(4, v.getPersonality());
        p.setString(5, v.getInterests());
        p.setString(6, blankToNull(v.getModelName()));
        p.setString(7, v.getReplyMode());
    }

    private void validate(AiCharacterEntity v) {
        if (v.getCharacterName() == null || v.getCharacterName().isBlank() || v.getSystemPrompt() == null
                || v.getSystemPrompt().isBlank())
            throw new IllegalArgumentException("名前と基本指示は必須です。");
        if (!"MENTION".equals(v.getReplyMode()) && !"ALWAYS".equals(v.getReplyMode()))
            throw new IllegalArgumentException("応答モードが不正です。");
        if (v.getUserId() != null && !v.getUserId().matches("[a-zA-Z0-9_-]{3,20}"))
            throw new IllegalArgumentException("ユーザーIDは半角英数字・_・-の3～20文字です。");
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private AiCharacterEntity map(ResultSet r) throws SQLException {
        return AiCharacterEntity.builder().characterId(r.getLong("character_id")).userId(r.getString("user_id"))
                .characterName(r.getString("character_name")).systemPrompt(r.getString("system_prompt"))
                .personality(r.getString("personality")).interests(r.getString("interests"))
                .modelName(r.getString("model_name")).replyMode(r.getString("reply_mode"))
                .activeFlg(r.getString("active_flg")).build();
    }
}
