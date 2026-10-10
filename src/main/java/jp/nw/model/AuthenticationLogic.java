package jp.nw.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jp.nw.entity.UserEntity;
import jp.nw.parts.DBBase;
import jp.nw.parts.PasswordUtil;

public class AuthenticationLogic {
    private static final int MAX_FAILURES = positiveEnv("LOGIN_MAX_FAILURES", 5);
    private static final int LOCK_MINUTES = positiveEnv("LOGIN_LOCK_MINUTES", 15);

    public Result authenticate(String userId, String rawPassword, HttpServletRequest request) {
        try (Connection con = new DBBase().getConnection()) {
            con.setAutoCommit(false);
            try {
                UserRecord record = findForUpdate(con, userId);
                Decision decision = evaluate(record, rawPassword, LocalDateTime.now(), MAX_FAILURES, LOCK_MINUTES);
                String ip = AuditLogLogic.clientIp(request);
                String agent = request == null ? null : request.getHeader("User-Agent");
                applyDecision(con, userId, decision, ip, agent);
                con.commit();
                // 失敗回数と監査ログは、メッセージマスタの設定不備があっても確定させる。
                String message = decision.outcome() == Outcome.SUCCESS ? null
                        : ErrorMessageLogic.find(con, decision.outcome().errorCode);
                return decision.toResult(userId, record, message);
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new CodedException.Failure(ErrorCode.APP_134, e);
        }
    }

    private UserRecord findForUpdate(Connection con, String userId) throws SQLException {
        String sql = "SELECT password,first_name,last_name,permission,password_expiration,delete_flg,"
                + "account_type,account_disabled,failed_login_count,locked_until,force_password_change "
                + "FROM users_info WHERE user_id=? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return null;
                UserRecord record = new UserRecord();
                record.password = rs.getString("password");
                record.firstName = rs.getString("first_name");
                record.lastName = rs.getString("last_name");
                record.permission = rs.getString("permission");
                record.passwordExpiration = rs.getString("password_expiration");
                record.deleteFlag = rs.getString("delete_flg");
                record.accountType = rs.getString("account_type");
                record.accountDisabled = rs.getBoolean("account_disabled");
                record.failedCount = rs.getInt("failed_login_count");
                Timestamp lockedUntil = rs.getTimestamp("locked_until");
                record.lockedUntil = lockedUntil == null ? null : lockedUntil.toLocalDateTime();
                record.forcePasswordChange = rs.getBoolean("force_password_change");
                return record;
            }
        }
    }

    static Decision evaluate(UserRecord record, String rawPassword, LocalDateTime now,
            int maxFailures, int lockMinutes) {
        if (record == null)
            return new Decision(Outcome.NO_USER, 0, null);
        if (record.accountDisabled || !"0".equals(record.deleteFlag))
            return new Decision(Outcome.DISABLED, 0, null);
        if ("AI".equals(record.accountType))
            return new Decision(Outcome.AI_ACCOUNT, 0, null);
        if (record.lockedUntil != null && record.lockedUntil.isAfter(now))
            return new Decision(Outcome.ALREADY_LOCKED, 0, record.lockedUntil);
        if (!PasswordUtil.matches(rawPassword == null ? "" : rawPassword, record.password)) {
            int failures = record.failedCount + 1;
            LocalDateTime lockUntil = failures >= maxFailures ? now.plusMinutes(lockMinutes) : null;
            return new Decision(lockUntil == null ? Outcome.BAD_PASSWORD : Outcome.NOW_LOCKED,
                    failures, lockUntil);
        }
        if (passwordExpired(record.passwordExpiration, now.toLocalDate()))
            return new Decision(Outcome.EXPIRED, 0, null);
        return new Decision(Outcome.SUCCESS, 0, null);
    }

    private void applyDecision(Connection con, String userId, Decision decision, String ip, String agent)
            throws SQLException {
        switch (decision.outcome()) {
            case BAD_PASSWORD, NOW_LOCKED -> {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE users_info SET failed_login_count=?,locked_until=? WHERE user_id=?")) {
                    ps.setInt(1, decision.lockUntil() == null ? decision.failures() : 0);
                    ps.setObject(2, decision.lockUntil());
                    ps.setString(3, userId);
                    ps.executeUpdate();
                }
            }
            case SUCCESS -> {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE users_info SET failed_login_count=0,locked_until=NULL,last_login_at=NOW() WHERE user_id=?")) {
                    ps.setString(1, userId);
                    ps.executeUpdate();
                }
            }
            default -> {
            }
        }
        AuditLogLogic.recordStructured(con, userId, "AUTH", decision.outcome().auditAction, "USER", userId,
                decision.outcome() == Outcome.SUCCESS, ip, agent, decision.outcome().name(), decision.auditData());
    }

    private static boolean passwordExpired(String expiration, LocalDate today) {
        try {
            return expiration != null && !"99999999".equals(expiration)
                    && today.isAfter(LocalDate.parse(expiration, DateTimeFormatter.BASIC_ISO_DATE));
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

    static class UserRecord {
        String password, firstName, lastName, permission, passwordExpiration, deleteFlag, accountType;
        boolean accountDisabled, forcePasswordChange;
        int failedCount;
        LocalDateTime lockedUntil;
    }

    enum Outcome {
        NO_USER("LOGIN_FAILURE", ErrorCode.AUTH_001),
        DISABLED("LOGIN_BLOCKED_DISABLED", ErrorCode.AUTH_002),
        AI_ACCOUNT("LOGIN_BLOCKED_AI", ErrorCode.AUTH_003),
        ALREADY_LOCKED("LOGIN_BLOCKED_LOCKED", ErrorCode.AUTH_004),
        BAD_PASSWORD("LOGIN_FAILURE", ErrorCode.AUTH_001),
        NOW_LOCKED("ACCOUNT_LOCKED", ErrorCode.AUTH_005),
        EXPIRED("LOGIN_BLOCKED_PASSWORD_EXPIRED", ErrorCode.AUTH_006),
        SUCCESS("LOGIN_SUCCESS", null);

        final String auditAction;
        final ErrorCode errorCode;

        Outcome(String auditAction, ErrorCode errorCode) {
            this.auditAction = auditAction;
            this.errorCode = errorCode;
        }
    }

    record Decision(Outcome outcome, int failures, LocalDateTime lockUntil) {
        Map<String, Object> auditData() {
            Map<String, Object> values = new LinkedHashMap<>();
            if (failures > 0)
                values.put("failedCount", failures);
            if (lockUntil != null)
                values.put("lockUntil", lockUntil.toString());
            return values;
        }

        Result toResult(String userId, UserRecord record, String message) {
            if (outcome != Outcome.SUCCESS)
                return Result.failure(outcome.errorCode.code(), message);
            UserEntity user = UserEntity.builder().userId(userId).firstName(record.firstName)
                    .lastName(record.lastName).permission(record.permission).accountType(record.accountType).build();
            return Result.success(user, record.forcePasswordChange);
        }
    }

    public record Result(boolean authenticated, UserEntity user, boolean forcePasswordChange,
            String errorCode, String message) {
        static Result success(UserEntity u, boolean force) {
            return new Result(true, u, force, null, null);
        }

        static Result failure(String code, String message) {
            return new Result(false, null, false, code, message);
        }
    }
}
