package jp.nw.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jp.nw.parts.DBBase;

/** DBに保存した、ユーザーごとに1つだけ有効なログイントークンを管理します。 */
public final class SecurityToken {

    private SecurityToken() {
    }

    public static boolean updateToken(String userId, String token) {
        if (isBlank(userId) || isBlank(token))
            return false;

        String sql = "UPDATE users_info SET current_login_token=? WHERE user_id=?";
        try (Connection connection = new DBBase().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, token);
            statement.setString(2, userId);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException("ログイントークンの更新に失敗しました。", e);
        }
    }

    /** DB上の最新トークンとセッションのトークンを一度のSQLで照合します。 */
    public static boolean matchesCurrentToken(String userId, String sessionToken) {
        if (isBlank(userId) || isBlank(sessionToken))
            return false;

        String sql = "SELECT 1 FROM users_info "
                + "WHERE user_id=? AND current_login_token=? "
                + "AND COALESCE(account_disabled, FALSE)=FALSE "
                + "AND COALESCE(delete_flg, '0')='0'";
        try (Connection connection = new DBBase().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, sessionToken);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("ログイントークンの照合に失敗しました。", e);
        }
    }

    /** 正規のログアウト時に、そのユーザーの全ログインセッションを失効させます。 */
    public static boolean revokeAllSessions(String userId) {
        if (isBlank(userId))
            return false;

        String sql = "UPDATE users_info SET current_login_token=NULL WHERE user_id=?";
        try (Connection connection = new DBBase().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException("ログイントークンの破棄に失敗しました。", e);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
