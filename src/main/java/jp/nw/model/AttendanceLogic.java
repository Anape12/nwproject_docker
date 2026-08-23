package jp.nw.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import jp.nw.entity.AttendanceEntity;
import jp.nw.parts.DBBase;

public class AttendanceLogic {
    private static final String SELECT="SELECT a.*,r.title AS report_title,r.status AS report_status FROM attendance_record a LEFT JOIN work_report r ON r.report_id=a.report_id ";
    public List<AttendanceEntity> findMonth(String userId,java.time.YearMonth month){
        String sql=SELECT+"WHERE a.user_id=? AND a.work_date>=? AND a.work_date<? ORDER BY a.work_date";DBBase db=new DBBase();
        try(Connection con=db.getConnection();PreparedStatement ps=con.prepareStatement(sql)){ps.setString(1,userId);ps.setObject(2,month.atDay(1));ps.setObject(3,month.plusMonths(1).atDay(1));try(ResultSet rs=ps.executeQuery()){List<AttendanceEntity> list=new ArrayList<>();while(rs.next())list.add(map(rs));return list;}}catch(SQLException e){throw new RuntimeException("勤怠情報の取得に失敗しました。",e);}
    }
    public AttendanceEntity findById(long id,String userId){String sql=SELECT+"WHERE a.attendance_id=? AND a.user_id=?";DBBase db=new DBBase();try(Connection con=db.getConnection();PreparedStatement ps=con.prepareStatement(sql)){ps.setLong(1,id);ps.setString(2,userId);try(ResultSet rs=ps.executeQuery()){return rs.next()?map(rs):null;}}catch(SQLException e){throw new RuntimeException("勤怠情報の取得に失敗しました。",e);}}
    public void save(AttendanceEntity value){
        validateReport(value);String sql=value.getAttendanceId()==0
                ?"INSERT INTO attendance_record(user_id,work_date,clock_in,clock_out,break_minutes,work_type,note,report_id) VALUES(?,?,?,?,?,?,?,?)"
                :"UPDATE attendance_record SET work_date=?,clock_in=?,clock_out=?,break_minutes=?,work_type=?,note=?,report_id=? WHERE attendance_id=? AND user_id=? AND approval_status IN ('DRAFT','REJECTED')";
        DBBase db=new DBBase();try(Connection con=db.getConnection();PreparedStatement ps=con.prepareStatement(sql)){int i=1;if(value.getAttendanceId()==0)ps.setString(i++,value.getUserId());ps.setObject(i++,value.getWorkDate());ps.setObject(i++,value.getClockIn());ps.setObject(i++,value.getClockOut());ps.setInt(i++,value.getBreakMinutes());ps.setString(i++,value.getWorkType());ps.setString(i++,value.getNote());if(value.getReportId()==null)ps.setNull(i++,java.sql.Types.BIGINT);else ps.setLong(i++,value.getReportId());if(value.getAttendanceId()!=0){ps.setLong(i++,value.getAttendanceId());ps.setString(i,value.getUserId());}if(ps.executeUpdate()!=1)throw new IllegalArgumentException("更新対象の勤怠情報が見つかりません。");}catch(java.sql.SQLIntegrityConstraintViolationException e){throw new IllegalArgumentException("同じ勤務日の勤怠は既に登録されています。");}catch(SQLException e){throw new RuntimeException("勤怠情報の保存に失敗しました。",e);}
    }
    public boolean delete(long id,String userId){DBBase db=new DBBase();try(Connection con=db.getConnection();PreparedStatement ps=con.prepareStatement("DELETE FROM attendance_record WHERE attendance_id=? AND user_id=? AND approval_status IN ('DRAFT','REJECTED')")){ps.setLong(1,id);ps.setString(2,userId);return ps.executeUpdate()==1;}catch(SQLException e){throw new RuntimeException("勤怠情報の削除に失敗しました。",e);}}
    private void validateReport(AttendanceEntity value){if(value.getReportId()==null)return;DBBase db=new DBBase();try(Connection con=db.getConnection();PreparedStatement ps=con.prepareStatement("SELECT 1 FROM work_report WHERE report_id=? AND author_id=? AND report_date=?")){ps.setLong(1,value.getReportId());ps.setString(2,value.getUserId());ps.setObject(3,value.getWorkDate());try(ResultSet rs=ps.executeQuery()){if(!rs.next())throw new IllegalArgumentException("同じ報告日の自分の報告書だけを関連付けできます。");}}catch(SQLException e){throw new RuntimeException("報告書の確認に失敗しました。",e);}}
    private AttendanceEntity map(ResultSet rs)throws SQLException{long report=rs.getLong("report_id");Long reportId=rs.wasNull()?null:report;return AttendanceEntity.builder().attendanceId(rs.getLong("attendance_id")).userId(rs.getString("user_id")).workDate(rs.getObject("work_date",java.time.LocalDate.class)).clockIn(rs.getObject("clock_in",java.time.LocalTime.class)).clockOut(rs.getObject("clock_out",java.time.LocalTime.class)).breakMinutes(rs.getInt("break_minutes")).workType(rs.getString("work_type")).note(rs.getString("note")).reportId(reportId).reportTitle(rs.getString("report_title")).reportStatus(rs.getString("report_status")).approvalStatus(rs.getString("approval_status")).build();}
}
