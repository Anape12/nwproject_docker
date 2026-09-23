package jp.nw.controller;

import java.io.IOException;
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
import jp.nw.model.UserSecurityLogic;

@WebServlet("/UserSecurityAdmin")
public class UserSecurityAdminController extends HttpServlet {
    protected void doGet(HttpServletRequest q, HttpServletResponse s) throws ServletException, IOException {
        HttpSession session = q.getSession();
        UserEntity u = user(session);
        if (!admin(u)) {
            s.sendError(403);
            return;
        }
        String token = (String) session.getAttribute("userSecurityCsrf");
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute("userSecurityCsrf", token);
        }
        q.setAttribute("csrfToken", token);
        q.setAttribute("users", new UserSecurityLogic().findAll());
        q.setAttribute("flash", session.getAttribute("securityFlash"));
        q.setAttribute("flashType", session.getAttribute("securityFlashType"));
        session.removeAttribute("securityFlash");
        session.removeAttribute("securityFlashType");
        q.getRequestDispatcher("/WEB-INF/jsp/security/userSecurityAdmin.jsp").forward(q, s);
    }

    protected void doPost(HttpServletRequest q, HttpServletResponse s) throws IOException {
        q.setCharacterEncoding("UTF-8");
        HttpSession session = q.getSession(false);
        UserEntity u = user(session);
        if (!admin(u)) {
            s.sendError(403);
            return;
        }
        if (!Objects.equals(session.getAttribute("userSecurityCsrf"), q.getParameter("csrfToken"))) {
            s.sendError(403);
            return;
        }
        String target = trim(q.getParameter("targetUserId")), action = q.getParameter("action");
        try {
            UserSecurityLogic l = new UserSecurityLogic();
            String ip = AuditLogLogic.clientIp(q), agent = q.getHeader("User-Agent");
            switch (action) {
                case "disable" -> l.setDisabled(u.getUserId(), target, true, ip, agent);
                case "enable" -> l.setDisabled(u.getUserId(), target, false, ip, agent);
                case "unlock" -> l.unlock(u.getUserId(), target, ip, agent);
                case "resetPassword" ->
                    l.resetPassword(u.getUserId(), target, q.getParameter("temporaryPassword"), ip, agent);
                case "updateProfile" -> l.updateProfile(u.getUserId(), target, trim(q.getParameter("firstName")),
                        trim(q.getParameter("lastName")), q.getParameter("permission"), ip, agent);
                default -> throw new IllegalArgumentException("操作が不正です。");
            }
            flash(session, "更新しました。", "success");
        } catch (Exception e) {
            AuditLogLogic.record(q, "SECURITY", action == null ? "UNKNOWN" : action.toUpperCase(), "USER", target,
                    false, e.getMessage());
            flash(session, e.getMessage() == null ? "更新に失敗しました。" : e.getMessage(), "error");
        }
        s.sendRedirect(q.getContextPath() + "/UserSecurityAdmin");
    }

    private UserEntity user(HttpSession s) {
        return s == null ? null : (UserEntity) s.getAttribute("loginUser");
    }

    private boolean admin(UserEntity u) {
        return u != null && "1".equals(u.getPermission());
    }

    private String trim(String v) {
        return v == null ? "" : v.trim();
    }

    private void flash(HttpSession s, String m, String t) {
        s.setAttribute("securityFlash", m);
        s.setAttribute("securityFlashType", t);
    }
}
