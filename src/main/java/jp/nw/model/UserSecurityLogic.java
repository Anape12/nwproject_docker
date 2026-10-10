package jp.nw.model;

import jp.nw.model.CodedException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jp.nw.entity.UserEntity;
import jp.nw.domain.user.PermissionAction;
import jp.nw.domain.user.PermissionStatus;
import jp.nw.parts.DBBase;
import jp.nw.parts.PasswordUtil;
import jp.nw.util.PermissionCheckUtil;

public class UserSecurityLogic {
    public List<UserEntity> findAll() {
        DBBase db = new DBBase();
        String sql = "SELECT id,user_id,first_name,last_name,permission,account_type,password_expiration,delete_flg,account_disabled,failed_login_count,locked_until,last_login_at,force_password_change FROM users_info ORDER BY account_type,user_id";
        try (Connection c = db.getConnection();
                PreparedStatement p = c.prepareStatement(sql);
                ResultSet r = p.executeQuery()) {
            List<UserEntity> list = new ArrayList<>();
            while (r.next())
                list.add(UserEntity.builder().id(r.getInt("id")).userId(r.getString("user_id"))
                        .firstName(r.getString("first_name")).lastName(r.getString("last_name"))
                        .permission(r.getString("permission")).accountType(r.getString("account_type"))
                        .passwordExpiration(r.getString("password_expiration")).deleteFlag(r.getString("delete_flg"))
                        .accountDisabled(r.getBoolean("account_disabled"))
                        .failedLoginCount(r.getInt("failed_login_count")).lockedUntil(local(r, "locked_until"))
                        .lastLoginAt(local(r, "last_login_at"))
                        .forcePasswordChange(r.getBoolean("force_password_change")).build());
            return list;
        } catch (SQLException e) {
            throw new CodedException.Failure("ERR00010101", e);
        }
    }

    public void updateProfile(String actor, String target, String first, String last, String permission, String ip,
            String agent) {
        if (first.isBlank() || last.isBlank())
            throw new CodedException.Validation("ERR00010102");
        if (PermissionStatus.fromValue(permission).isEmpty())
            throw new CodedException.Validation("ERR00010103");
        DBBase db = new DBBase();
        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try {
                String oldFirst, oldLast, oldPermission;
                try (PreparedStatement p = c.prepareStatement(
                        "SELECT first_name,last_name,permission,account_type FROM users_info WHERE user_id=? FOR UPDATE")) {
                    p.setString(1, target);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next() || "AI".equalsIgnoreCase(r.getString("account_type")))
                            throw new CodedException.Validation("ERR00010104");
                        oldFirst = r.getString(1);
                        oldLast = r.getString(2);
                        oldPermission = r.getString(3);
                    }
                }
                authorize(c, actor, oldPermission, permission);
                if (actor.equals(target) && !oldPermission.equals(permission))
                    throw new CodedException.Validation("ERR00010105");
                try (PreparedStatement p = c.prepareStatement(
                        "UPDATE users_info SET first_name=?,last_name=?,current_login_token=CASE WHEN permission<>? THEN NULL ELSE current_login_token END,permission=? WHERE user_id=?")) {
                    p.setString(1, first);
                    p.setString(2, last);
                    p.setString(3, permission);
                    p.setString(4, permission);
                    p.setString(5, target);
                    p.executeUpdate();
                }
                if (!Objects.equals(oldFirst, first) || !Objects.equals(oldLast, last))
                    AuditLogLogic.record(c, actor, "USER", "USER_INFO_CHANGED", "USER", target, true, ip, agent,
                            "氏名=" + oldLast + " " + oldFirst + " → " + last + " " + first);
                if (!Objects.equals(oldPermission, permission))
                    AuditLogLogic.record(c, actor, "SECURITY", "PERMISSION_CHANGED", "USER", target, true, ip, agent,
                            "権限=" + oldPermission + " → " + permission);
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new CodedException.Failure("ERR00010106", e);
        }
    }

    public void setDisabled(String actor, String target, boolean disabled, String ip, String agent) {
        if (actor.equals(target) && disabled)
            throw new CodedException.Validation("ERR00010107");
        execute(actor, target, "ACCOUNT_" + (disabled ? "DISABLED" : "ENABLED"), ip, agent, c -> {
            try (PreparedStatement p = c.prepareStatement(
                    "UPDATE users_info SET account_disabled=?,delete_flg=?,current_login_token=NULL WHERE user_id=? AND account_type<>'AI'")) {
                p.setBoolean(1, disabled);
                p.setString(2, disabled ? "1" : "0");
                p.setString(3, target);
                if (p.executeUpdate() != 1)
                    throw new CodedException.Validation("ERR00010108");
            }
        });
    }

    public void unlock(String actor, String target, String ip, String agent) {
        execute(actor, target, "ACCOUNT_UNLOCKED", ip, agent, c -> {
            try (PreparedStatement p = c
                    .prepareStatement("UPDATE users_info SET failed_login_count=0,locked_until=NULL WHERE user_id=?")) {
                p.setString(1, target);
                if (p.executeUpdate() != 1)
                    throw new CodedException.Validation("ERR00010109");
            }
        });
    }

    public void resetPassword(String actor, String target, String password, String ip, String agent) {
        if (password == null || password.length() < 8 || password.length() > 72 || !password.matches(".*[A-Za-z].*")
                || !password.matches(".*[0-9].*"))
            throw new CodedException.Validation("ERR00010110");
        execute(actor, target, "PASSWORD_RESET", ip, agent, c -> {
            try (PreparedStatement p = c.prepareStatement(
                    "UPDATE users_info SET password=?,password_changed_at=NOW(),password_expiration=?,force_password_change=TRUE,failed_login_count=0,locked_until=NULL,current_login_token=NULL WHERE user_id=? AND account_type<>'AI'")) {
                p.setString(1, PasswordUtil.encode(password));
                p.setString(2, LocalDate.now().plusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE));
                p.setString(3, target);
                if (p.executeUpdate() != 1)
                    throw new CodedException.Validation("ERR00010111");
            }
        });
    }

    private void execute(String actor, String target, String action, String ip, String agent, SqlWork work) {
        DBBase db = new DBBase();
        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try {
                String targetPermission;
                try (PreparedStatement p = c.prepareStatement(
                        "SELECT permission,account_type FROM users_info WHERE user_id=? FOR UPDATE")) {
                    p.setString(1, target);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next() || "AI".equalsIgnoreCase(r.getString("account_type")))
                            throw new CodedException.Validation("ERR00010108");
                        targetPermission = r.getString("permission");
                    }
                }
                authorize(c, actor, targetPermission, null);
                work.run(c);
                AuditLogLogic.record(c, actor, "SECURITY", action, "USER", target, true, ip, agent, null);
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new CodedException.Failure("ERR00010112", e);
        }
    }

    private void authorize(Connection c, String actorId, String targetPermission, String newPermission)
            throws SQLException {
        UserEntity actor;
        try (PreparedStatement p = c.prepareStatement(
                "SELECT permission,account_type FROM users_info WHERE user_id=? AND delete_flg='0' AND account_disabled=FALSE")) {
            p.setString(1, actorId);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new CodedException.Validation("ERR00010113");
                actor = UserEntity.builder().permission(r.getString("permission"))
                        .accountType(r.getString("account_type")).build();
            }
        }
        if (!PermissionCheckUtil.can(actor, PermissionAction.USER_MANAGE)
                || !PermissionCheckUtil.canManageRole(actor, targetPermission)
                || (newPermission != null && !PermissionCheckUtil.canManageRole(actor, newPermission)))
            throw new CodedException.Validation("ERR00010114");
    }

    private java.time.LocalDateTime local(ResultSet r, String c) throws SQLException {
        Timestamp t = r.getTimestamp(c);
        return t == null ? null : t.toLocalDateTime();
    }

    @FunctionalInterface
    private interface SqlWork {
        void run(Connection c) throws Exception;
    }
}
