package jp.nw.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

import jp.nw.parts.DBBase;

/** Resolves user-facing messages from the database catalog. */
public final class ErrorMessageLogic {
    private static final Pattern CODE_FORMAT = Pattern.compile("(?:ERR|WAR)[0-9]{8}");

    private ErrorMessageLogic() {
    }

    public static String find(Connection connection, String errorCode) throws SQLException {
        if (!isValidCode(errorCode)) {
            throw new IllegalArgumentException("エラーコードはERRまたはWARと8桁の数字で指定してください。");
        }
        String sql = "SELECT message_text FROM error_message_mst WHERE error_code=? AND enabled=TRUE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, errorCode);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    String message = result.getString(1);
                    if (message != null && !message.isBlank()) return message;
                }
            }
        }
        throw new SQLException("有効なエラーメッセージが登録されていません: " + errorCode);
    }

    static boolean isValidCode(String errorCode) {
        return errorCode != null && CODE_FORMAT.matcher(errorCode).matches();
    }

    public static String get(String errorCode) {
        try (Connection connection = new DBBase().getConnection()) {
            return find(connection, errorCode);
        } catch (SQLException e) {
            throw new IllegalStateException("エラーメッセージを取得できません: " + errorCode, e);
        }
    }

    /** Never show an arbitrary exception message (including SQL details) to a user. */
    public static String forDisplay(Throwable error) {
        return get(errorCode(error));
    }

    static String errorCode(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof CodedException.Coded coded) return coded.errorCode();
            current = current.getCause();
        }
        return "ERR00010000";
    }
}
