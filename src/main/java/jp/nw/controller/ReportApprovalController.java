package jp.nw.controller;

import jp.nw.model.CodedException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.nw.entity.ApprovalRequestEntity;
import jp.nw.entity.UserEntity;
import jp.nw.domain.user.PermissionAction;
import jp.nw.model.ApprovalLogic;
import jp.nw.util.PermissionCheckUtil;

@WebServlet("/ReportApproval")
public class ReportApprovalController extends HttpServlet {
    protected void doGet(HttpServletRequest q, HttpServletResponse s) throws ServletException, IOException {
        HttpSession session = q.getSession();
        UserEntity u = (UserEntity) session.getAttribute("loginUser");
        if (!reviewer(u)) {
            s.sendError(403);
            return;
        }
        ApprovalLogic l = new ApprovalLogic();
        String token = (String) session.getAttribute("approvalCsrfToken");
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute("approvalCsrfToken", token);
        }
        q.setAttribute("applications", l.findAll());
        q.setAttribute("canConfigureApprovals", PermissionCheckUtil.can(u, PermissionAction.APPROVAL_CONFIGURE));
        ApprovalRequestEntity selected = selected(q.getParameter("id"), l);
        q.setAttribute("selected", selected);
        if (selected != null)
            q.setAttribute("approvalHistory", l.history(selected.getApprovalId()));
        q.setAttribute("csrfToken", token);
        q.setAttribute("flashMessage", session.getAttribute("approvalFlash"));
        q.setAttribute("flashType", session.getAttribute("approvalFlashType"));
        session.removeAttribute("approvalFlash");
        session.removeAttribute("approvalFlashType");
        q.getRequestDispatcher("/WEB-INF/jsp/report/reportApproval.jsp").forward(q, s);
    }

    protected void doPost(HttpServletRequest q, HttpServletResponse s) throws IOException {
        q.setCharacterEncoding("UTF-8");
        HttpSession session = q.getSession(false);
        UserEntity u = (UserEntity) session.getAttribute("loginUser");
        if (!reviewer(u)) {
            s.sendError(403);
            return;
        }
        if (!Objects.equals(session.getAttribute("approvalCsrfToken"), q.getParameter("csrfToken"))) {
            s.sendError(403);
            return;
        }
        try {
            ApprovalLogic l = new ApprovalLogic();
            String action = q.getParameter("action");
            if ("route".equals(action) && PermissionCheckUtil.can(u, PermissionAction.APPROVAL_CONFIGURE))
                l.saveRoute(q.getParameter("applicationType"), Integer.parseInt(q.getParameter("requiredSteps")));
            else if ("delegate".equals(action) && PermissionCheckUtil.can(u, PermissionAction.APPROVAL_CONFIGURE))
                l.addDelegate(u.getUserId(), q.getParameter("delegateUserId"),
                        LocalDate.parse(q.getParameter("validFrom")), LocalDate.parse(q.getParameter("validTo")));
            else if ("route".equals(action) || "delegate".equals(action))
                throw new CodedException.Denied("ERR00010020");
            else {
                String decision = q.getParameter("decision"), comment = trim(q.getParameter("comment"));
                if ("REJECTED".equals(decision) && comment.isBlank())
                    throw new CodedException.Validation("ERR00010021");
                String[] values = q.getParameterValues("approvalIds");
                if (values != null) {
                    List<Long> ids = new ArrayList<>();
                    for (String v : values)
                        ids.add(Long.valueOf(v));
                    l.reviewBatch(ids, u, decision, comment);
                } else
                    l.review(Long.parseLong(q.getParameter("approvalId")), u, decision, comment);
            }
            flash(session, "処理が完了しました。", "success");
        } catch (Exception e) {
            flash(session, jp.nw.model.ErrorMessageLogic.forDisplay(e), "error");
        }
        s.sendRedirect(q.getContextPath() + "/ReportApproval");
    }

    private ApprovalRequestEntity selected(String v, ApprovalLogic l) {
        try {
            return v == null ? null : l.findById(Long.parseLong(v));
        } catch (Exception e) {
            return null;
        }
    }

    private boolean reviewer(UserEntity u) {
        return new ApprovalLogic().canReview(u);
    }

    private String trim(String v) {
        return v == null ? "" : v.trim();
    }

    private void flash(HttpSession s, String m, String t) {
        s.setAttribute("approvalFlash", m);
        s.setAttribute("approvalFlashType", t);
    }
}
