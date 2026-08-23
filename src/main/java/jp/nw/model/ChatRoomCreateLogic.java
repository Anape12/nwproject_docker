package jp.nw.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jp.nw.entity.UserEntity;
import jp.nw.parts.DBBase;

/** 1対1チャットルームの作成に関するDB処理。 */
public class ChatRoomCreateLogic {

    public List<UserEntity> getAvailableUsers(String loginUserId) {
        String sql = "SELECT user_id, first_name, last_name "
                + "FROM users_info "
                + "WHERE user_id <> ? AND delete_flg = '0' "
                + "ORDER BY last_name, first_name, user_id";

        List<UserEntity> users = new ArrayList<>();
        DBBase dbBase = new DBBase();
        try (Connection connection = dbBase.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, loginUserId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    users.add(UserEntity.builder()
                            .userId(result.getString("user_id"))
                            .firstName(result.getString("first_name"))
                            .lastName(result.getString("last_name"))
                            .build());
                }
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("チャット対象ユーザーの取得に失敗しました。", e);
        }
    }

    public CreatedRoom createOrGetDirectRoom(String loginUserId, String targetUserId) {
        if (targetUserId == null || targetUserId.isBlank() || loginUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("チャット対象ユーザーが不正です。");
        }

        DBBase dbBase = new DBBase();
        try (Connection connection = dbBase.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String displayName = lockAndGetTargetUser(connection, loginUserId, targetUserId);
                String existingRoomId = findDirectRoom(connection, loginUserId, targetUserId);
                if (existingRoomId != null) {
                    connection.commit();
                    return new CreatedRoom(existingRoomId, displayName);
                }

                String roomId = UUID.randomUUID().toString();
                insertRoom(connection, roomId, loginUserId);
                insertMember(connection, roomId, loginUserId);
                insertMember(connection, roomId, targetUserId);
                connection.commit();
                return new CreatedRoom(roomId, displayName);
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("チャットルームの作成に失敗しました。", e);
        }
    }

    private String lockAndGetTargetUser(Connection connection, String loginUserId, String targetUserId)
            throws SQLException {
        String sql = "SELECT user_id, first_name, last_name FROM users_info "
                + "WHERE user_id IN (?, ?) AND delete_flg = '0' ORDER BY user_id FOR UPDATE";
        String displayName = null;
        int found = 0;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, loginUserId);
            statement.setString(2, targetUserId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    found++;
                    if (targetUserId.equals(result.getString("user_id"))) {
                        displayName = result.getString("last_name") + " " + result.getString("first_name");
                    }
                }
            }
        }
        if (found != 2 || displayName == null) {
            throw new IllegalArgumentException("指定したユーザーは利用できません。");
        }
        return displayName;
    }

    private String findDirectRoom(Connection connection, String loginUserId, String targetUserId)
            throws SQLException {
        String sql = "SELECT r.room_id FROM chat_room r "
                + "INNER JOIN chat_room_member m ON m.room_id = r.room_id "
                + "WHERE r.room_type = '1' AND r.delete_flg = '0' "
                + "GROUP BY r.room_id "
                + "HAVING COUNT(*) = 2 AND SUM(m.user_id = ?) = 1 AND SUM(m.user_id = ?) = 1 "
                + "LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, loginUserId);
            statement.setString(2, targetUserId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString("room_id") : null;
            }
        }
    }

    private void insertRoom(Connection connection, String roomId, String createdById) throws SQLException {
        String sql = "INSERT INTO chat_room (room_id, room_name, room_type, created_by_id) VALUES (?, NULL, '1', ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, roomId);
            statement.setString(2, createdById);
            statement.executeUpdate();
        }
    }

    private void insertMember(Connection connection, String roomId, String userId) throws SQLException {
        String sql = "INSERT INTO chat_room_member (room_id, user_id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, roomId);
            statement.setString(2, userId);
            statement.executeUpdate();
        }
    }

    public record CreatedRoom(String roomId, String displayName) {
    }
}
