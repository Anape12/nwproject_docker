package jp.nw.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.nw.entity.UserEntity;
import jp.nw.model.AuditLogLogic;
import jp.nw.parts.DBBase;
import jp.nw.parts.PasswordUtil;

@WebServlet("/ChangePassword")
public class ChangePasswordController extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession s = req.getSession();
        String token = (String) s.getAttribute("passwordCsrfToken");
        if (token == null) {
            token = UUID.randomUUID().toString();
            s.setAttribute("passwordCsrfToken", token);
        }
        req.setAttribute("csrfToken", token);
        req.getRequestDispatcher("/WEB-INF/jsp/security/changePassword.jsp").forward(req, res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession s = req.getSession(false);
        UserEntity u = (UserEntity) s.getAttribute("loginUser");
        if (!Objects.equals(s.getAttribute("passwordCsrfToken"), req.getParameter("csrfToken"))) {
            res.sendError(403);
            return;
        }
        String current = req.getParameter("currentPassword"), next = req.getParameter("newPassword"),
                confirm = req.getParameter("confirmation");
        try {
            validate(next, confirm);
            DBBase db = new DBBase();
            try (Connection c = db.getConnection()) {
                c.setAutoCommit(false);
                try {
                    String encoded;
                    try (PreparedStatement p = c
                            .prepareStatement("SELECT password FROM users_info WHERE user_id=? FOR UPDATE")) {
                        p.setString(1, u.getUserId());
                        try (var r = p.executeQuery()) {
                            if (!r.next() || !PasswordUtil.matches(current, r.getString(1)))
                                throw new IllegalArgumentException("現在のパスワードが正しくありません。");
                            encoded = r.getString(1);
                        }
                    }
                    if (PasswordUtil.matches(next, encoded))
                        throw new IllegalArgumentException("現在と異なるパスワードを指定してください。");
                    try (PreparedStatement p = c.prepareStatement(
                            "UPDATE users_info SET password=?,password_changed_at=NOW(),password_expiration=?,force_password_change=FALSE WHERE user_id=?")) {
                        p.setString(1, PasswordUtil.encode(next));
                        p.setString(2, LocalDate.now().plusDays(90).format(DateTimeFormatter.BASIC_ISO_DATE));
                        p.setString(3, u.getUserId());
                        p.executeUpdate();
                    }
                    AuditLogLogic.record(c, u.getUserId(), "SECURITY", "PASSWORD_CHANGED", "USER", u.getUserId(), true,
                            AuditLogLogic.clientIp(req), req.getHeader("User-Agent"), "本人による変更");
                    c.commit();
                } catch (Exception e) {
                    c.rollback();
                    throw e;
                }
            }
            s.setAttribute("forcePasswordChange", false);
            s.setAttribute("passwordChangedMessage", "パスワードを変更しました。");
            res.sendRedirect(req.getContextPath() + "/MenuSelect");
        } catch (Exception e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("csrfToken", s.getAttribute("passwordCsrfToken"));
            req.getRequestDispatcher("/WEB-INF/jsp/security/changePassword.jsp").forward(req, res);
        }
    }

    private void validate(String p, String c) {
        if (p == null || p.length() < 8 || p.length() > 72 || !p.matches(".*[A-Za-z].*") || !p.matches(".*[0-9].*"))
            throw new IllegalArgumentException("英字と数字を含む8～72文字で入力してください。");
        if (!p.equals(c))
            throw new IllegalArgumentException("確認用パスワードが一致しません。");
    }
}
