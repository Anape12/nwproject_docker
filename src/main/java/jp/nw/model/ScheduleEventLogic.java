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
        String sql = "SELECT DISTINCT e.* FROM schedule_event e LEFT JOIN schedule_participant p ON p.event_id=e.event_id WHERE (e.user_id=? OR e.visibility='SHARED' OR p.user_id=?) AND e.start_at < ? AND e.end_at > ? ORDER BY e.start_at,e.end_at,e.event_id";
        List<ScheduleEventEntity> events = new ArrayList<>();
        DBBase db = new DBBase();
        try (Connection con = db.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1,userId);ps.setString(2,userId);
            ps.setTimestamp(3, Timestamp.valueOf(end));
            ps.setTimestamp(4, Timestamp.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) events.add(map(rs)); }
            return events;
        } catch (SQLException e) { throw new RuntimeException("予定の取得に失敗しました。", e); }
    }

    public ScheduleEventEntity findById(long id, String userId) {
        String sql = "SELECT DISTINCT e.* FROM schedule_event e LEFT JOIN schedule_participant p ON p.event_id=e.event_id WHERE e.event_id=? AND (e.user_id=? OR e.visibility='SHARED' OR p.user_id=?)";
        DBBase db = new DBBase();
        try (Connection con = db.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1,id);ps.setString(2,userId);ps.setString(3,userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw new RuntimeException("予定の取得に失敗しました。", e); }
    }

    public long create(ScheduleEventEntity event) {
        String sql = "INSERT INTO schedule_event (user_id,title,description,start_at,end_at,all_day,color,visibility,recurrence_rule,recurrence_until) VALUES (?,?,?,?,?,?,?,?,?,?)";
        DBBase db = new DBBase();
        try (Connection con = db.getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int i = 1; ps.setString(i++, event.getUserId()); i = bind(ps, event, i);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) return keys.getLong(1); }
            throw new SQLException("Generated key not found.");
        } catch (SQLException e) { throw new RuntimeException("予定の登録に失敗しました。", e); }
    }

    public boolean update(ScheduleEventEntity event) {
        String sql = "UPDATE schedule_event SET title=?,description=?,start_at=?,end_at=?,all_day=?,color=?,visibility=?,recurrence_rule=?,recurrence_until=? WHERE event_id=? AND user_id=?";
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

    public List<java.util.Map<String,String>> users(String except){DBBase db=new DBBase();try(Connection c=db.getConnection();PreparedStatement p=c.prepareStatement("SELECT user_id,CONCAT(last_name,' ',first_name) name FROM users_info WHERE delete_flg='0' AND user_id<>? ORDER BY last_name,first_name")){p.setString(1,except);try(ResultSet r=p.executeQuery()){List<java.util.Map<String,String>> list=new ArrayList<>();while(r.next())list.add(java.util.Map.of("id",r.getString(1),"name",r.getString(2)));return list;}}catch(SQLException e){throw new RuntimeException(e);}}
    public void invite(long eventId,String[] users){if(users==null)return;DBBase db=new DBBase();try(Connection c=db.getConnection();PreparedStatement p=c.prepareStatement("INSERT IGNORE INTO schedule_participant(event_id,user_id) VALUES(?,?)")){for(String user:users){p.setLong(1,eventId);p.setString(2,user);p.addBatch();}p.executeBatch();PortalLogic portal=new PortalLogic();for(String user:users)portal.notifyUser(c,user,"SCHEDULE","予定への招待","新しい予定へ招待されました。","/OpenCalender?edit="+eventId);}catch(SQLException e){throw new RuntimeException("参加者の招待に失敗しました。",e);}}

    public boolean isParticipant(long eventId,String user){DBBase db=new DBBase();try(Connection c=db.getConnection();PreparedStatement p=c.prepareStatement("SELECT 1 FROM schedule_participant WHERE event_id=? AND user_id=?")){p.setLong(1,eventId);p.setString(2,user);try(ResultSet r=p.executeQuery()){return r.next();}}catch(SQLException e){throw new RuntimeException(e);}}
    public void respond(long eventId,String user,String status){if(!java.util.Set.of("ACCEPTED","DECLINED").contains(status))throw new IllegalArgumentException("回答が不正です。");DBBase db=new DBBase();try(Connection c=db.getConnection();PreparedStatement p=c.prepareStatement("UPDATE schedule_participant SET response_status=?,responded_at=NOW() WHERE event_id=? AND user_id=?")){p.setString(1,status);p.setLong(2,eventId);p.setString(3,user);if(p.executeUpdate()!=1)throw new IllegalArgumentException("招待が見つかりません。");}catch(SQLException e){throw new RuntimeException(e);}}

    private int bind(PreparedStatement ps, ScheduleEventEntity event, int i) throws SQLException {
        ps.setString(i++, event.getTitle()); ps.setString(i++, event.getDescription());
        ps.setTimestamp(i++, Timestamp.valueOf(event.getStartAt())); ps.setTimestamp(i++, Timestamp.valueOf(event.getEndAt()));
        ps.setBoolean(i++, event.isAllDay()); ps.setString(i++, event.getColor());ps.setString(i++,event.getVisibility()==null?"PRIVATE":event.getVisibility());ps.setString(i++,event.getRecurrenceRule());ps.setObject(i++,event.getRecurrenceUntil()); return i;
    }

    private ScheduleEventEntity map(ResultSet rs) throws SQLException {
        return ScheduleEventEntity.builder().eventId(rs.getLong("event_id")).userId(rs.getString("user_id"))
                .title(rs.getString("title")).description(rs.getString("description"))
                .startAt(rs.getTimestamp("start_at").toLocalDateTime()).endAt(rs.getTimestamp("end_at").toLocalDateTime())
                .allDay(rs.getBoolean("all_day")).color(rs.getString("color")).visibility(rs.getString("visibility")).recurrenceRule(rs.getString("recurrence_rule")).recurrenceUntil(rs.getObject("recurrence_until",java.time.LocalDate.class))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime()).updatedAt(rs.getTimestamp("updated_at").toLocalDateTime()).build();
    }
}
