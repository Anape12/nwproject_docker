package jp.nw.controller;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.nw.entity.AiCharacterEntity;
import jp.nw.entity.UserEntity;
import jp.nw.model.AiCharacterLogic;

@WebServlet("/AiCharacterAdmin")
public class AiCharacterAdminController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest q, HttpServletResponse s) throws ServletException, IOException {
        UserEntity u = user(q);
        if (u == null || !"1".equals(u.getPermission())) {
            s.sendError(403);
            return;
        }
        HttpSession session = q.getSession();
        if (session.getAttribute("aiAdminCsrf") == null)
            session.setAttribute("aiAdminCsrf", UUID.randomUUID().toString());
        AiCharacterLogic l = new AiCharacterLogic();
        q.setAttribute("characters", l.findAll());
        q.setAttribute("recentJobs", l.findRecentJobs());
        String edit = q.getParameter("edit");
        if (edit != null)
            try {
                q.setAttribute("selected", l.find(Long.parseLong(edit)));
            } catch (NumberFormatException ignored) {
            }
        q.setAttribute("flash", session.getAttribute("aiAdminFlash"));
        q.setAttribute("flashType", session.getAttribute("aiAdminFlashType"));
        session.removeAttribute("aiAdminFlash");
        session.removeAttribute("aiAdminFlashType");
        q.getRequestDispatcher("/WEB-INF/jsp/ai/characterAdmin.jsp").forward(q, s);
    }

    protected void doPost(HttpServletRequest q, HttpServletResponse s) throws IOException {
        q.setCharacterEncoding("UTF-8");
        UserEntity u = user(q);
        HttpSession session = q.getSession(false);
        if (u == null || !"1".equals(u.getPermission())) {
            s.sendError(403);
            return;
        }
        if (!java.util.Objects.equals(session.getAttribute("aiAdminCsrf"), q.getParameter("csrfToken"))) {
            s.sendError(403);
            return;
        }
        try {
            AiCharacterEntity v = AiCharacterEntity.builder().userId(trim(q.getParameter("userId")))
                    .characterName(trim(q.getParameter("characterName")))
                    .systemPrompt(trim(q.getParameter("systemPrompt"))).personality(trim(q.getParameter("personality")))
                    .interests(trim(q.getParameter("interests"))).modelName(trim(q.getParameter("modelName")))
                    .replyMode(q.getParameter("replyMode")).activeFlg(q.getParameter("activeFlg") == null ? "0" : "1")
                    .build();
            String id = q.getParameter("characterId");
            AiCharacterLogic l = new AiCharacterLogic();
            if (id == null || id.isBlank())
                l.create(v);
            else {
                v.setCharacterId(Long.parseLong(id));
                l.update(v);
            }
            flash(session, "AI住人を保存しました。", "success");
        } catch (Exception e) {
            flash(session, e.getMessage() == null ? "保存に失敗しました。" : e.getMessage(), "error");
        }
        s.sendRedirect(q.getContextPath() + "/AiCharacterAdmin");
    }

    private UserEntity user(HttpServletRequest q) {
        HttpSession s = q.getSession(false);
        return s == null ? null : (UserEntity) s.getAttribute("loginUser");
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private void flash(HttpSession s, String m, String t) {
        s.setAttribute("aiAdminFlash", m);
        s.setAttribute("aiAdminFlashType", t);
    }
}
