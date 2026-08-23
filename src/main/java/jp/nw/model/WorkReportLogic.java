package jp.nw.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import jp.nw.entity.WorkReportEntity;
import jp.nw.parts.DBBase;

public class WorkReportLogic {
    private static final String SELECT_COLUMNS = "SELECT r.*, CONCAT(a.last_name, ' ', a.first_name) AS author_name, "
            + "CONCAT(v.last_name, ' ', v.first_name) AS reviewer_name FROM work_report r "
            + "INNER JOIN users_info a ON a.user_id=r.author_id LEFT JOIN users_info v ON v.user_id=r.reviewed_by_id ";

    public List<WorkReportEntity> findOwn(String userId) {
        return queryList(SELECT_COLUMNS + "WHERE r.author_id=? ORDER BY r.report_date DESC, r.updated_at DESC", userId);
    }

    public WorkReportEntity findOwnById(long id, String userId) {
        return queryOne(SELECT_COLUMNS + "WHERE r.report_id=? AND r.author_id=?", id, userId);
    }

    public WorkReportEntity findForReview(long id) {
        return queryOne(SELECT_COLUMNS + "WHERE r.report_id=?", id);
    }

    public List<WorkReportEntity> findApprovalQueue() {
        String sql = SELECT_COLUMNS + "ORDER BY CASE r.status WHEN 'SUBMITTED' THEN 0 ELSE 1 END, r.submitted_at ASC, r.updated_at DESC";
        DBBase db = new DBBase();
        try (Connection con=db.getConnection(); PreparedStatement ps=con.prepareStatement(sql); ResultSet rs=ps.executeQuery()) {
            List<WorkReportEntity> reports=new ArrayList<>(); while(rs.next()) reports.add(map(rs)); return reports;
        } catch(SQLException e){ throw new RuntimeException("報告書一覧の取得に失敗しました。",e); }
    }

    public long create(WorkReportEntity report) {
        String sql="INSERT INTO work_report(author_id,report_date,title,body,status) VALUES(?,?,?,?,'DRAFT')";
        DBBase db=new DBBase();
        try(Connection con=db.getConnection();PreparedStatement ps=con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1,report.getAuthorId());ps.setObject(2,report.getReportDate());ps.setString(3,report.getTitle());ps.setString(4,report.getBody());ps.executeUpdate();
            try(ResultSet keys=ps.getGeneratedKeys()){if(keys.next())return keys.getLong(1);}throw new SQLException("Generated key not found.");
        }catch(SQLException e){throw new RuntimeException("報告書の保存に失敗しました。",e);}
    }

    public boolean update(WorkReportEntity report) {
        String sql="UPDATE work_report SET report_date=?,title=?,body=? WHERE report_id=? AND author_id=? AND status IN ('DRAFT','REJECTED')";
        DBBase db=new DBBase();
        try(Connection con=db.getConnection();PreparedStatement ps=con.prepareStatement(sql)){
            ps.setObject(1,report.getReportDate());ps.setString(2,report.getTitle());ps.setString(3,report.getBody());ps.setLong(4,report.getReportId());ps.setString(5,report.getAuthorId());return ps.executeUpdate()==1;
        }catch(SQLException e){throw new RuntimeException("報告書の更新に失敗しました。",e);}
    }

    public boolean submit(long id,String userId){
        String sql="UPDATE work_report SET status='SUBMITTED',submitted_at=NOW(),reviewed_at=NULL,reviewed_by_id=NULL,review_comment=NULL WHERE report_id=? AND author_id=? AND status IN ('DRAFT','REJECTED')";
        return executeOwned(sql,id,userId,"報告書の提出に失敗しました。");
    }

    public boolean delete(long id,String userId){
        String sql="DELETE FROM work_report WHERE report_id=? AND author_id=? AND status IN ('DRAFT','REJECTED')";
        return executeOwned(sql,id,userId,"報告書の削除に失敗しました。");
    }

    public void review(long id,String reviewerId,String decision,String comment){
        DBBase db=new DBBase();
        try(Connection con=db.getConnection()){
            con.setAutoCommit(false);
            try{
                try(PreparedStatement lock=con.prepareStatement("SELECT status FROM work_report WHERE report_id=? FOR UPDATE")){
                    lock.setLong(1,id);try(ResultSet rs=lock.executeQuery()){if(!rs.next()||!"SUBMITTED".equals(rs.getString("status")))throw new IllegalArgumentException("この報告書は承認待ちではありません。");}
                }
                try(PreparedStatement update=con.prepareStatement("UPDATE work_report SET status=?,reviewed_at=NOW(),reviewed_by_id=?,review_comment=? WHERE report_id=?")){
                    update.setString(1,decision);update.setString(2,reviewerId);update.setString(3,comment);update.setLong(4,id);update.executeUpdate();
                }
                try(PreparedStatement history=con.prepareStatement("INSERT INTO work_report_review(report_id,reviewer_id,decision,comment) VALUES(?,?,?,?)")){
                    history.setLong(1,id);history.setString(2,reviewerId);history.setString(3,decision);history.setString(4,comment);history.executeUpdate();
                }
                con.commit();
            }catch(Exception e){con.rollback();throw e;}
        }catch(IllegalArgumentException e){throw e;}catch(Exception e){throw new RuntimeException("承認処理に失敗しました。",e);}
    }

    private boolean executeOwned(String sql,long id,String userId,String message){
        DBBase db=new DBBase();try(Connection con=db.getConnection();PreparedStatement ps=con.prepareStatement(sql)){ps.setLong(1,id);ps.setString(2,userId);return ps.executeUpdate()==1;}catch(SQLException e){throw new RuntimeException(message,e);}
    }
    private List<WorkReportEntity> queryList(String sql,Object...params){
        DBBase db=new DBBase();try(Connection con=db.getConnection();PreparedStatement ps=con.prepareStatement(sql)){bind(ps,params);try(ResultSet rs=ps.executeQuery()){List<WorkReportEntity> list=new ArrayList<>();while(rs.next())list.add(map(rs));return list;}}catch(SQLException e){throw new RuntimeException("報告書の取得に失敗しました。",e);}
    }
    private WorkReportEntity queryOne(String sql,Object...params){List<WorkReportEntity> list=queryList(sql,params);return list.isEmpty()?null:list.get(0);}
    private void bind(PreparedStatement ps,Object...params)throws SQLException{for(int i=0;i<params.length;i++)ps.setObject(i+1,params[i]);}
    private WorkReportEntity map(ResultSet rs)throws SQLException{
        return WorkReportEntity.builder().reportId(rs.getLong("report_id")).authorId(rs.getString("author_id")).authorName(rs.getString("author_name"))
                .reportDate(rs.getObject("report_date",java.time.LocalDate.class)).title(rs.getString("title")).body(rs.getString("body")).status(rs.getString("status"))
                .submittedAt(local(rs,"submitted_at")).reviewedAt(local(rs,"reviewed_at")).reviewedById(rs.getString("reviewed_by_id")).reviewerName(rs.getString("reviewer_name"))
                .reviewComment(rs.getString("review_comment")).createdAt(local(rs,"created_at")).updatedAt(local(rs,"updated_at")).build();
    }
    private java.time.LocalDateTime local(ResultSet rs,String name)throws SQLException{Timestamp value=rs.getTimestamp(name);return value==null?null:value.toLocalDateTime();}
}
