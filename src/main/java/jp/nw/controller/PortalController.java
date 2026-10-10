package jp.nw.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.nw.entity.UserEntity;
import jp.nw.domain.user.PermissionAction;
import jp.nw.model.PortalLogic;
import jp.nw.util.PermissionCheckUtil;

@WebServlet("/Portal")
public class PortalController extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        UserEntity u = (UserEntity) req.getSession().getAttribute("loginUser");
        PortalLogic l = new PortalLogic();
        req.setAttribute("dashboard", l.dashboard(u.getUserId(), PermissionCheckUtil.can(u, PermissionAction.ADMIN_DASHBOARD_VIEW)));
        req.setAttribute("query", req.getParameter("q"));
        req.setAttribute("searchResults", l.search(u.getUserId(), req.getParameter("q")));
        req.getRequestDispatcher("/WEB-INF/jsp/portal/dashboard.jsp").forward(req, res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        UserEntity u = (UserEntity) req.getSession().getAttribute("loginUser");
        try {
            if (req.getParameter("approvalId") != null)
                new jp.nw.model.ApprovalLogic().withdraw(Long.parseLong(req.getParameter("approvalId")), u.getUserId());
            else
                new PortalLogic().markRead(u.getUserId(), Long.parseLong(req.getParameter("notificationId")));
        } catch (Exception e) {
            req.getSession().setAttribute("portalError", jp.nw.model.ErrorMessageLogic.forDisplay(e));
        }
        res.sendRedirect(req.getContextPath() + "/Portal");
    }
}
