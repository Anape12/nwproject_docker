package jp.nw.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jp.nw.dto.ThreadCommentDto;
import jp.nw.dto.ThreadDto;
import jp.nw.parts.DBBase;

public class ThreadDao extends DBBase {

        public List<ThreadDto> findAll() {

                List<ThreadDto> list = new ArrayList<>();

                String sql = "SELECT thread_id, title, author_id, thread_content, status, closed_by_id, closed_at " +
                                "FROM thread_info " +
                                "ORDER BY status = 'OPEN' DESC, updated_at DESC, thread_id DESC";

                try {

                        PreparedStatement ps = getConnection().prepareStatement(sql);

                        ResultSet rs = ps.executeQuery();

                        while (rs.next()) {

                                ThreadDto thread = new ThreadDto();

                                thread.setThreadId(
                                                rs.getInt("thread_id"));

                                thread.setTitle(
                                                rs.getString("title"));

                                thread.setAuthorId(
                                                rs.getString("author_id"));

                                thread.setContent(
                                                rs.getString("thread_content"));
                                applyStatus(thread, rs);

                                list.add(thread);
                        }

                } catch (SQLException e) {
                        throw new RuntimeException("スレッド一覧の取得に失敗しました。", e);
                }

                return list;
        }

        public ThreadDto findById(int threadId) {

                ThreadDto dto = null;

                String sql = "SELECT * FROM thread_info WHERE thread_id = ?";

                try {

                        PreparedStatement ps = getConnection().prepareStatement(sql);

                        ps.setInt(1, threadId);

                        ResultSet rs = ps.executeQuery();

                        if (rs.next()) {

                                dto = new ThreadDto();

                                dto.setThreadId(
                                                rs.getInt("thread_id"));

                                dto.setTitle(
                                                rs.getString("title"));

                                dto.setAuthorId(
                                                rs.getString("author_id"));

                                dto.setThreadContent(
                                                rs.getString("thread_content"));
                                dto.setContent(rs.getString("thread_content"));
                                applyStatus(dto, rs);
                        }

                } catch (SQLException e) {
                        throw new RuntimeException("スレッドの取得に失敗しました。", e);
                }

                return dto;
        }

        public List<ThreadCommentDto> findComments(int threadId) {

                List<ThreadCommentDto> list = new ArrayList<>();

                String sql = "SELECT * FROM thread_comment " +
                                "WHERE thread_id = ? " +
                                "ORDER BY created_at";

                try {

                        PreparedStatement ps = getConnection().prepareStatement(sql);

                        ps.setInt(1, threadId);

                        ResultSet rs = ps.executeQuery();

                        while (rs.next()) {
                                ThreadCommentDto dto = new ThreadCommentDto();

                                dto.setCommentId(
                                                rs.getInt("comment_id"));

                                dto.setThreadId(
                                                rs.getInt("thread_id"));

                                dto.setAuthorId(
                                                rs.getString("author_id"));

                                dto.setCommentText(
                                                rs.getString("comment_text"));

                                dto.setCreatedAt(
                                                rs.getTimestamp("created_at"));

                                list.add(dto);
                        }

                } catch (SQLException e) {
                        throw new RuntimeException("コメントの取得に失敗しました。", e);
                }

                return list;
        }

        public boolean insertComment(ThreadCommentDto dto) {

                String sql = "INSERT INTO thread_comment (thread_id, author_id, comment_text) "
                                + "SELECT thread_id, ?, ? FROM thread_info "
                                + "WHERE thread_id = ? AND status = 'OPEN'";

                try {

                        PreparedStatement ps = getConnection().prepareStatement(sql);

                        ps.setString(1,
                                        dto.getAuthorId());
                        ps.setString(2,
                                        dto.getCommentText());
                        ps.setInt(3, dto.getThreadId());

                        return ps.executeUpdate() == 1;

                } catch (SQLException e) {
                        throw new RuntimeException("コメントの投稿に失敗しました。", e);
                }
        }

        public boolean updateStatus(int threadId, String userId, boolean admin, String status) {
                String sql = "UPDATE thread_info SET status = ?, "
                                + "closed_by_id = CASE WHEN ? = 'CLOSED' THEN ? ELSE NULL END, "
                                + "closed_at = CASE WHEN ? = 'CLOSED' THEN CURRENT_TIMESTAMP ELSE NULL END "
                                + "WHERE thread_id = ? AND (author_id = ? OR ? = 1)";
                try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                        ps.setString(1, status);
                        ps.setString(2, status);
                        ps.setString(3, userId);
                        ps.setString(4, status);
                        ps.setInt(5, threadId);
                        ps.setString(6, userId);
                        ps.setInt(7, admin ? 1 : 0);
                        return ps.executeUpdate() == 1;
                } catch (SQLException e) {
                        throw new RuntimeException("スレッド状態の更新に失敗しました。", e);
                }
        }

        private void applyStatus(ThreadDto dto, ResultSet rs) throws SQLException {
                dto.setStatus(rs.getString("status"));
                dto.setClosedById(rs.getString("closed_by_id"));
                dto.setClosedAt(rs.getTimestamp("closed_at"));
        }
}
