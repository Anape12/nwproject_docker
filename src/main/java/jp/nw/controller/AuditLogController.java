package jp.nw.controller;

import java.io.IOException;
import java.time.LocalDate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.nw.entity.UserEntity;
import jp.nw.model.AuditLogLogic;

@WebServlet("/AuditLog")
public class AuditLogController extends HttpServlet {
    protected void doGet(HttpServletRequest q, HttpServletResponse s) throws ServletException, IOException {
        UserEntity u = (UserEntity) q.getSession().getAttribute("loginUser");
        if (u == null || !"1".equals(u.getPermission())) {
            s.sendError(403);
            return;
        }
        String user = trim(q.getParameter("userId")), category = trim(q.getParameter("category")),
                action = trim(q.getParameter("action"));
        Boolean success = "true".equals(q.getParameter("success")) ? Boolean.TRUE
                : "false".equals(q.getParameter("success")) ? Boolean.FALSE : null;
        LocalDate from = date(q.getParameter("from")), to = date(q.getParameter("to"));
        q.setAttribute("logs", AuditLogLogic.search(user, category, action, success, from, to, 500));
        q.getRequestDispatcher("/WEB-INF/jsp/security/auditLog.jsp").forward(q, s);
    }

    private String trim(String v) {
        return v == null ? "" : v.trim();
    }

    private LocalDate date(String v) {
        try {
            return v == null || v.isBlank() ? null : LocalDate.parse(v);
        } catch (Exception e) {
            return null;
        }
    }
}
