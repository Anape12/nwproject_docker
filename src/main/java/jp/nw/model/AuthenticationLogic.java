package jp.nw.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import jp.nw.entity.UserEntity;
import jp.nw.parts.DBBase;
import jp.nw.parts.PasswordUtil;
import jp.nw.parts.Query;
import jp.nw.parts.SqlType;

public class AuthenticationLogic {
    private static final int MAX_FAILURES = positiveEnv("LOGIN_MAX_FAILURES", 5);
    private static final int LOCK_MINUTES = positiveEnv("LOGIN_LOCK_MINUTES", 15);

    private DBBase dbCon = null;

    public Result authenticate(String userId, String rawPassword, HttpServletRequest request) {
        DBBase db = new DBBase();
        try (Connection con = db.getConnection()) {
            con.setAutoCommit(false);
            try {
                UserRecord record = findForUpdate(con, userId);
                String ip = AuditLogLogic.clientIp(request);
                String agent = request.getHeader("User-Agent");
                if (record == null) {
                    AuditLogLogic.record(con, userId, "AUTH", "LOGIN_FAILURE", "USER", userId, false, ip, agent,
                            "ユーザーIDまたはパスワードが不正");
                    con.commit();
                    return Result.failure("ユーザーIDまたはパスワードが正しくありません。");
                }
                if (record.accountDisabled || !"0".equals(record.deleteFlag)) {
                    AuditLogLogic.record(con, userId, "AUTH", "LOGIN_BLOCKED_DISABLED", "USER", userId, false, ip,
                            agent, "無効アカウント");
                    con.commit();
                    return Result.failure("このアカウントは利用できません。管理者へお問い合わせください。");
                }
                if ("AI".equals(record.accountType)) {
                    AuditLogLogic.record(con, userId, "AUTH", "LOGIN_BLOCKED_AI", "USER", userId, false, ip, agent,
                            "AIアカウント");
                    con.commit();
                    return Result.failure("このアカウントではログインできません。");
                }
                if (record.lockedUntil != null && record.lockedUntil.isAfter(LocalDateTime.now())) {
                    AuditLogLogic.record(con, userId, "AUTH", "LOGIN_BLOCKED_LOCKED", "USER", userId, false, ip, agent,
                            "ロック期限=" + record.lockedUntil);
                    con.commit();
                    return Result.failure("ログイン失敗が規定回数を超えたため、一時的にロックされています。");
                }
                if (!PasswordUtil.matches(rawPassword == null ? "" : rawPassword, record.password)) {
                    int failures = record.failedCount + 1;
                    LocalDateTime lock = failures >= MAX_FAILURES ? LocalDateTime.now().plusMinutes(LOCK_MINUTES)
                            : null;
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE users_info SET failed_login_count=?,locked_until=? WHERE user_id=?")) {
                        ps.setInt(1, failures >= MAX_FAILURES ? 0 : failures);
                        ps.setObject(2, lock);
                        ps.setString(3, userId);
                        ps.executeUpdate();
                    }
                    AuditLogLogic.record(con, userId, "AUTH", lock == null ? "LOGIN_FAILURE" : "ACCOUNT_LOCKED", "USER",
                            userId, false, ip, agent, "失敗回数=" + failures);
                    con.commit();
                    return Result
                            .failure(lock == null ? "ユーザーIDまたはパスワードが正しくありません。" : "ログイン失敗が規定回数を超えたため、一時的にロックされました。");
                }
                if (passwordExpired(record.passwordExpiration)) {
                    AuditLogLogic.record(con, userId, "AUTH", "LOGIN_BLOCKED_PASSWORD_EXPIRED", "USER", userId, false,
                            ip, agent, "パスワード有効期限切れ");
                    con.commit();
                    return Result.failure("パスワードの有効期限が切れています。管理者へ再設定を依頼してください。");
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE users_info SET failed_login_count=0,locked_until=NULL,last_login_at=NOW() WHERE user_id=?")) {
                    ps.setString(1, userId);
                    ps.executeUpdate();
                }
                AuditLogLogic.record(con, userId, "AUTH", "LOGIN_SUCCESS", "USER", userId, true, ip, agent, null);
                con.commit();
                UserEntity user = UserEntity.builder().userId(userId).firstName(record.firstName)
                        .lastName(record.lastName)
                        .permission(record.permission).accountType(record.accountType).build();
                return Result.success(user, record.forcePasswordChange);
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("ログイン処理に失敗しました。", e);
        }
    }

    private UserRecord findForUpdate(Connection con, String userId) throws SQLException {
        LinkedHashMap<String, Object> conditions = new LinkedHashMap<>();
        conditions.put("user_id", userId);
        Query query = Query.builder().sqlType(SqlType.SELECT).tableName("users_info")
                .selectColumns(List.of("user_id", "password",
                        "first_name", "last_name", "permission", "password_expiration", "delete_flg", "account_type",
                        "account_disabled", "failed_login_count", "locked_until", "force_password_change"))
                .conditions(conditions).build();

        this.dbCon = new DBBase();
        List<Object> resultList = (List<Object>) this.dbCon.execute(query);

        if (resultList.isEmpty()) {
            return null;
        }

        UserRecord record = new UserRecord();
        HashMap<String, Object> resultMap = (HashMap<String, Object>) resultList.get(0);
        record.password = (String) resultMap.get("password");
        record.firstName = (String) resultMap.get("first_name");
        record.lastName = (String) resultMap.get("last_name");
        record.permission = (String) resultMap.get("permission");
        record.passwordExpiration = (String) resultMap.get("password_expiration");
        record.deleteFlag = (String) resultMap.get("delete_flg");
        record.accountType = (String) resultMap.get("account_type");
        record.accountDisabled = (Boolean) resultMap.get("account_disabled");
        record.failedCount = (Integer) resultMap.get("failed_login_count");
        record.lockedUntil = (LocalDateTime) resultMap.get("locked_until");
        record.forcePasswordChange = (Boolean) resultMap.get("force_password_change");

        return record;
    }

    private boolean passwordExpired(String expiration) {
        try {
            return expiration != null && !"99999999".equals(expiration)
                    && LocalDate.now().isAfter(LocalDate.parse(expiration, DateTimeFormatter.BASIC_ISO_DATE));
        } catch (Exception e) {
            return true;
        }
    }

    private static int positiveEnv(String name, int fallback) {
        try {
            int v = Integer.parseInt(System.getenv(name));
            return v > 0 ? v : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private static class UserRecord {
        String password, firstName, lastName, permission, passwordExpiration, deleteFlag, accountType;
        boolean accountDisabled, forcePasswordChange;
        int failedCount;
        LocalDateTime lockedUntil;
    }

    public record Result(boolean authenticated, UserEntity user, boolean forcePasswordChange, String message) {
        static Result success(UserEntity u, boolean force) {
            return new Result(true, u, force, null);
        }

        static Result failure(String m) {
            return new Result(false, null, false, m);
        }
    }
}
