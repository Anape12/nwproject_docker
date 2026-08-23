package jp.nw.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jp.nw.entity.ScheduleEventEntity;
import jp.nw.parts.DBBase;

public class ScheduleEventLogic {
    public List<ScheduleEventEntity> findInRange(String userId, LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT * FROM schedule_event WHERE user_id = ? AND start_at < ? AND end_at > ? ORDER BY start_at, end_at, event_id";
        List<ScheduleEventEntity> events = new ArrayList<>();
        DBBase db = new DBBase();
        try (Connection con = db.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(end));
            ps.setTimestamp(3, Timestamp.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) events.add(map(rs)); }
            return events;
        } catch (SQLException e) { throw new RuntimeException("予定の取得に失敗しました。", e); }
    }

    public ScheduleEventEntity findById(long id, String userId) {
        String sql = "SELECT * FROM schedule_event WHERE event_id = ? AND user_id = ?";
        DBBase db = new DBBase();
        try (Connection con = db.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id); ps.setString(2, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw new RuntimeException("予定の取得に失敗しました。", e); }
    }

    public long create(ScheduleEventEntity event) {
        String sql = "INSERT INTO schedule_event (user_id,title,description,start_at,end_at,all_day,color) VALUES (?,?,?,?,?,?,?)";
        DBBase db = new DBBase();
        try (Connection con = db.getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int i = 1; ps.setString(i++, event.getUserId()); i = bind(ps, event, i);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) return keys.getLong(1); }
            throw new SQLException("Generated key not found.");
        } catch (SQLException e) { throw new RuntimeException("予定の登録に失敗しました。", e); }
    }

    public boolean update(ScheduleEventEntity event) {
        String sql = "UPDATE schedule_event SET title=?,description=?,start_at=?,end_at=?,all_day=?,color=? WHERE event_id=? AND user_id=?";
        DBBase db = new DBBase();
        try (Connection con = db.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            int i = bind(ps, event, 1); ps.setLong(i++, event.getEventId()); ps.setString(i, event.getUserId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) { throw new RuntimeException("予定の更新に失敗しました。", e); }
    }

    public boolean delete(long id, String userId) {
        String sql = "DELETE FROM schedule_event WHERE event_id=? AND user_id=?";
        DBBase db = new DBBase();
        try (Connection con = db.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id); ps.setString(2, userId); return ps.executeUpdate() == 1;
        } catch (SQLException e) { throw new RuntimeException("予定の削除に失敗しました。", e); }
    }

    private int bind(PreparedStatement ps, ScheduleEventEntity event, int i) throws SQLException {
        ps.setString(i++, event.getTitle()); ps.setString(i++, event.getDescription());
        ps.setTimestamp(i++, Timestamp.valueOf(event.getStartAt())); ps.setTimestamp(i++, Timestamp.valueOf(event.getEndAt()));
        ps.setBoolean(i++, event.isAllDay()); ps.setString(i++, event.getColor()); return i;
    }

    private ScheduleEventEntity map(ResultSet rs) throws SQLException {
        return ScheduleEventEntity.builder().eventId(rs.getLong("event_id")).userId(rs.getString("user_id"))
                .title(rs.getString("title")).description(rs.getString("description"))
                .startAt(rs.getTimestamp("start_at").toLocalDateTime()).endAt(rs.getTimestamp("end_at").toLocalDateTime())
                .allDay(rs.getBoolean("all_day")).color(rs.getString("color"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime()).updatedAt(rs.getTimestamp("updated_at").toLocalDateTime()).build();
    }
}
