package jp.nw.model;

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
import java.util.Set;

import jp.nw.entity.UserEntity;
import jp.nw.parts.DBBase;
import jp.nw.parts.PasswordUtil;

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
            throw new RuntimeException("ユーザー一覧の取得に失敗しました。", e);
        }
    }

    public void updateProfile(String actor, String target, String first, String last, String permission, String ip,
            String agent) {
        if (first.isBlank() || last.isBlank())
            throw new IllegalArgumentException("姓と名を入力してください。");
        if (!Set.of("1", "2").contains(permission))
            throw new IllegalArgumentException("権限が不正です。");
        DBBase db = new DBBase();
        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try {
                String oldFirst, oldLast, oldPermission;
                try (PreparedStatement p = c.prepareStatement(
                        "SELECT first_name,last_name,permission FROM users_info WHERE user_id=? FOR UPDATE")) {
                    p.setString(1, target);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next())
                            throw new IllegalArgumentException("ユーザーが見つかりません。");
                        oldFirst = r.getString(1);
                        oldLast = r.getString(2);
                        oldPermission = r.getString(3);
                    }
                }
                if (actor.equals(target) && !oldPermission.equals(permission))
                    throw new IllegalArgumentException("ログイン中の自分自身の権限は変更できません。");
                try (PreparedStatement p = c.prepareStatement(
                        "UPDATE users_info SET first_name=?,last_name=?,permission=? WHERE user_id=?")) {
                    p.setString(1, first);
                    p.setString(2, last);
                    p.setString(3, permission);
                    p.setString(4, target);
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
            throw new RuntimeException("ユーザー情報の変更に失敗しました。", e);
        }
    }

    public void setDisabled(String actor, String target, boolean disabled, String ip, String agent) {
        if (actor.equals(target) && disabled)
            throw new IllegalArgumentException("自分自身を無効化できません。");
        execute(actor, target, "ACCOUNT_" + (disabled ? "DISABLED" : "ENABLED"), ip, agent, c -> {
            try (PreparedStatement p = c.prepareStatement(
                    "UPDATE users_info SET account_disabled=?,delete_flg=?,current_login_token=NULL WHERE user_id=? AND account_type<>'AI'")) {
                p.setBoolean(1, disabled);
                p.setString(2, disabled ? "1" : "0");
                p.setString(3, target);
                if (p.executeUpdate() != 1)
                    throw new IllegalArgumentException("対象アカウントを変更できません。");
            }
        });
    }

    public void unlock(String actor, String target, String ip, String agent) {
        execute(actor, target, "ACCOUNT_UNLOCKED", ip, agent, c -> {
            try (PreparedStatement p = c
                    .prepareStatement("UPDATE users_info SET failed_login_count=0,locked_until=NULL WHERE user_id=?")) {
                p.setString(1, target);
                if (p.executeUpdate() != 1)
                    throw new IllegalArgumentException("対象アカウントがありません。");
            }
        });
    }

    public void resetPassword(String actor, String target, String password, String ip, String agent) {
        if (password == null || password.length() < 8 || password.length() > 72 || !password.matches(".*[A-Za-z].*")
                || !password.matches(".*[0-9].*"))
            throw new IllegalArgumentException("仮パスワードは英字と数字を含む8～72文字で入力してください。");
        execute(actor, target, "PASSWORD_RESET", ip, agent, c -> {
            try (PreparedStatement p = c.prepareStatement(
                    "UPDATE users_info SET password=?,password_changed_at=NOW(),password_expiration=?,force_password_change=TRUE,failed_login_count=0,locked_until=NULL,current_login_token=NULL WHERE user_id=? AND account_type<>'AI'")) {
                p.setString(1, PasswordUtil.encode(password));
                p.setString(2, LocalDate.now().plusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE));
                p.setString(3, target);
                if (p.executeUpdate() != 1)
                    throw new IllegalArgumentException("対象アカウントを再設定できません。");
            }
        });
    }

    private void execute(String actor, String target, String action, String ip, String agent, SqlWork work) {
        DBBase db = new DBBase();
        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try {
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
            throw new RuntimeException("セキュリティ設定の更新に失敗しました。", e);
        }
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
