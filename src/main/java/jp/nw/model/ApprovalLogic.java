package jp.nw.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import jp.nw.entity.ApprovalRequestEntity;
import jp.nw.parts.DBBase;

public class ApprovalLogic {
    private static final String SELECT="SELECT ar.*,CONCAT(u.last_name,' ',u.first_name) applicant_name,CONCAT(v.last_name,' ',v.first_name) reviewer_name,"
            +"CASE WHEN ar.application_type='REPORT' THEN wr.report_date ELSE at.work_date END target_date,"
            +"CASE WHEN ar.application_type='REPORT' THEN wr.title ELSE CONCAT('勤怠 ',at.work_date) END title,"
            +"CASE WHEN ar.application_type='REPORT' THEN wr.body ELSE CONCAT(COALESCE(TIME_FORMAT(at.clock_in,'%H:%i'),'-'),' ～ ',COALESCE(TIME_FORMAT(at.clock_out,'%H:%i'),'-'),' / ',at.work_type,' / 休憩',at.break_minutes,'分',IF(at.note IS NULL OR at.note='','',CONCAT(' / ',at.note))) END detail "
            +"FROM approval_request ar INNER JOIN users_info u ON u.user_id=ar.applicant_id LEFT JOIN users_info v ON v.user_id=ar.reviewer_id "
            +"LEFT JOIN work_report wr ON ar.application_type='REPORT' AND wr.report_id=ar.target_id LEFT JOIN attendance_record at ON ar.application_type='ATTENDANCE' AND at.attendance_id=ar.target_id ";
    public List<ApprovalRequestEntity> findAll(){return query(SELECT+"ORDER BY CASE ar.status WHEN 'SUBMITTED' THEN 0 ELSE 1 END,ar.submitted_at ASC,ar.updated_at DESC");}
    public ApprovalRequestEntity findById(long id){List<ApprovalRequestEntity> list=query(SELECT+"WHERE ar.approval_id=?",id);return list.isEmpty()?null:list.get(0);}
    public void submit(String type,long targetId,String applicantId){
        if(!"REPORT".equals(type)&&!"ATTENDANCE".equals(type))throw new IllegalArgumentException("申請種別が不正です。");DBBase db=new DBBase();
        try(Connection con=db.getConnection()){con.setAutoCommit(false);try{
            String table="REPORT".equals(type)?"work_report":"attendance_record";String idCol="REPORT".equals(type)?"report_id":"attendance_id";String ownerCol="REPORT".equals(type)?"author_id":"user_id";String statusCol="REPORT".equals(type)?"status":"approval_status";
            try(PreparedStatement target=con.prepareStatement("UPDATE "+table+" SET "+statusCol+"='SUBMITTED' WHERE "+idCol+"=? AND "+ownerCol+"=? AND "+statusCol+" IN ('DRAFT','REJECTED')")){target.setLong(1,targetId);target.setString(2,applicantId);if(target.executeUpdate()!=1)throw new IllegalArgumentException("申請できるデータが見つかりません。");}
            long approvalId;
            try(PreparedStatement upsert=con.prepareStatement("INSERT INTO approval_request(application_type,target_id,applicant_id,status,submitted_at) VALUES(?,?,?,'SUBMITTED',NOW()) ON DUPLICATE KEY UPDATE approval_id=LAST_INSERT_ID(approval_id),status='SUBMITTED',submitted_at=NOW(),reviewed_at=NULL,reviewer_id=NULL,review_comment=NULL",java.sql.Statement.RETURN_GENERATED_KEYS)){upsert.setString(1,type);upsert.setLong(2,targetId);upsert.setString(3,applicantId);upsert.executeUpdate();try(ResultSet keys=upsert.getGeneratedKeys()){if(!keys.next())throw new SQLException("Approval key not found.");approvalId=keys.getLong(1);}}
            history(con,approvalId,"SUBMITTED",applicantId,null);con.commit();
        }catch(Exception e){con.rollback();throw e;}}catch(IllegalArgumentException e){throw e;}catch(Exception e){throw new RuntimeException("承認申請に失敗しました。",e);}
    }
    public int submitAttendanceMonth(String applicantId,java.time.YearMonth month){
        DBBase db=new DBBase();
        try(Connection con=db.getConnection()){con.setAutoCommit(false);try{
            List<Long> targetIds=new ArrayList<>();
            try(PreparedStatement ps=con.prepareStatement("SELECT attendance_id FROM attendance_record WHERE user_id=? AND work_date>=? AND work_date<? AND approval_status IN ('DRAFT','REJECTED') ORDER BY work_date FOR UPDATE")){
                ps.setString(1,applicantId);ps.setObject(2,month.atDay(1));ps.setObject(3,month.plusMonths(1).atDay(1));try(ResultSet rs=ps.executeQuery()){while(rs.next())targetIds.add(rs.getLong(1));}
            }
            if(targetIds.isEmpty())throw new IllegalArgumentException("この月に申請できる勤怠がありません。");
            for(long targetId:targetIds){
                try(PreparedStatement update=con.prepareStatement("UPDATE attendance_record SET approval_status='SUBMITTED' WHERE attendance_id=? AND user_id=? AND approval_status IN ('DRAFT','REJECTED')")){update.setLong(1,targetId);update.setString(2,applicantId);if(update.executeUpdate()!=1)throw new IllegalArgumentException("勤怠の状態が変更されたため一括申請できませんでした。");}
                long approvalId;
                try(PreparedStatement upsert=con.prepareStatement("INSERT INTO approval_request(application_type,target_id,applicant_id,status,submitted_at) VALUES('ATTENDANCE',?,?,'SUBMITTED',NOW()) ON DUPLICATE KEY UPDATE approval_id=LAST_INSERT_ID(approval_id),status='SUBMITTED',submitted_at=NOW(),reviewed_at=NULL,reviewer_id=NULL,review_comment=NULL",java.sql.Statement.RETURN_GENERATED_KEYS)){
                    upsert.setLong(1,targetId);upsert.setString(2,applicantId);upsert.executeUpdate();try(ResultSet keys=upsert.getGeneratedKeys()){if(!keys.next())throw new SQLException("Approval key not found.");approvalId=keys.getLong(1);}
                }
                history(con,approvalId,"SUBMITTED",applicantId,"月次一括申請（"+month+"）");
            }
            con.commit();return targetIds.size();
        }catch(Exception e){con.rollback();throw e;}}
        catch(IllegalArgumentException e){throw e;}catch(Exception e){throw new RuntimeException("勤怠の月次一括申請に失敗しました。",e);}
    }
    public void review(long approvalId,String reviewerId,String decision,String comment){
        if(!"APPROVED".equals(decision)&&!"REJECTED".equals(decision))throw new IllegalArgumentException("承認結果が不正です。");DBBase db=new DBBase();
        try(Connection con=db.getConnection()){con.setAutoCommit(false);try{
            String type;long targetId;try(PreparedStatement lock=con.prepareStatement("SELECT application_type,target_id,status FROM approval_request WHERE approval_id=? FOR UPDATE")){lock.setLong(1,approvalId);try(ResultSet rs=lock.executeQuery()){if(!rs.next()||!"SUBMITTED".equals(rs.getString("status")))throw new IllegalArgumentException("この申請は承認待ちではありません。");type=rs.getString("application_type");targetId=rs.getLong("target_id");}}
            try(PreparedStatement ps=con.prepareStatement("UPDATE approval_request SET status=?,reviewed_at=NOW(),reviewer_id=?,review_comment=? WHERE approval_id=?")){ps.setString(1,decision);ps.setString(2,reviewerId);ps.setString(3,comment);ps.setLong(4,approvalId);ps.executeUpdate();}
            if("REPORT".equals(type)){try(PreparedStatement ps=con.prepareStatement("UPDATE work_report SET status=?,reviewed_at=NOW(),reviewed_by_id=?,review_comment=? WHERE report_id=?")){ps.setString(1,decision);ps.setString(2,reviewerId);ps.setString(3,comment);ps.setLong(4,targetId);ps.executeUpdate();}}
            else{try(PreparedStatement ps=con.prepareStatement("UPDATE attendance_record SET approval_status=? WHERE attendance_id=?")){ps.setString(1,decision);ps.setLong(2,targetId);ps.executeUpdate();}}
            history(con,approvalId,decision,reviewerId,comment);con.commit();
        }catch(Exception e){con.rollback();throw e;}}catch(IllegalArgumentException e){throw e;}catch(Exception e){throw new RuntimeException("承認処理に失敗しました。",e);}
    }
    private void history(Connection con,long id,String action,String user,String comment)throws SQLException{try(PreparedStatement ps=con.prepareStatement("INSERT INTO approval_history(approval_id,action,acted_by_id,comment) VALUES(?,?,?,?)")){ps.setLong(1,id);ps.setString(2,action);ps.setString(3,user);ps.setString(4,comment);ps.executeUpdate();}}
    private List<ApprovalRequestEntity> query(String sql,Object...args){DBBase db=new DBBase();try(Connection con=db.getConnection();PreparedStatement ps=con.prepareStatement(sql)){for(int i=0;i<args.length;i++)ps.setObject(i+1,args[i]);try(ResultSet rs=ps.executeQuery()){List<ApprovalRequestEntity> list=new ArrayList<>();while(rs.next())list.add(map(rs));return list;}}catch(SQLException e){throw new RuntimeException("申請一覧の取得に失敗しました。",e);}}
    private ApprovalRequestEntity map(ResultSet rs)throws SQLException{return ApprovalRequestEntity.builder().approvalId(rs.getLong("approval_id")).applicationType(rs.getString("application_type")).targetId(rs.getLong("target_id")).applicantId(rs.getString("applicant_id")).applicantName(rs.getString("applicant_name")).status(rs.getString("status")).targetDate(rs.getObject("target_date",java.time.LocalDate.class)).title(rs.getString("title")).detail(rs.getString("detail")).submittedAt(local(rs,"submitted_at")).reviewedAt(local(rs,"reviewed_at")).reviewerId(rs.getString("reviewer_id")).reviewerName(rs.getString("reviewer_name")).reviewComment(rs.getString("review_comment")).build();}
    private java.time.LocalDateTime local(ResultSet rs,String name)throws SQLException{Timestamp value=rs.getTimestamp(name);return value==null?null:value.toLocalDateTime();}
}
