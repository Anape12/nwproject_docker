package jp.nw.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import jp.nw.entity.UserEntity;
import jp.nw.parts.DBBase;
import jp.nw.parts.PasswordUtil;

public class UserInsertLogic {

    public void insert(UserEntity user, LocalDate passwordExpiration) {
        if (!permissionExists(user.getPermission())) {
            throw new IllegalArgumentException("指定した権限は利用できません。");
        }

        String sql = "INSERT INTO users_info "
                + "(user_id, password, birthday, permission, password_expiration, delete_flg, first_name, last_name) "
                + "VALUES (?, ?, ?, ?, ?, '0', ?, ?)";
        DBBase db = new DBBase();
        try (Connection connection = db.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUserId());
            statement.setString(2, PasswordUtil.encode(user.getPassword()));
            statement.setObject(3, LocalDate.parse(user.getBirthDate()));
            statement.setString(4, user.getPermission());
            statement.setString(5, passwordExpiration.format(DateTimeFormatter.BASIC_ISO_DATE));
            statement.setString(6, user.getFirstName());
            statement.setString(7, user.getLastName());
            statement.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("このユーザーIDは既に登録されています。");
        } catch (SQLException e) {
            throw new RuntimeException("ユーザー情報の登録に失敗しました。", e);
        }
    }

    public boolean userIdExists(String userId) {
        String sql = "SELECT 1 FROM users_info WHERE user_id = ?";
        DBBase db = new DBBase();
        try (Connection connection = db.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            try (ResultSet result = statement.executeQuery()) { return result.next(); }
        } catch (SQLException e) {
            throw new RuntimeException("ユーザーIDの確認に失敗しました。", e);
        }
    }

    private boolean permissionExists(String permission) {
        String sql = "SELECT 1 FROM permission_mst WHERE permission_id = ? AND delete_flg = '0'";
        DBBase db = new DBBase();
        try (Connection connection = db.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, permission);
            try (ResultSet result = statement.executeQuery()) { return result.next(); }
        } catch (SQLException e) {
            throw new RuntimeException("権限情報の確認に失敗しました。", e);
        }
    }
}
