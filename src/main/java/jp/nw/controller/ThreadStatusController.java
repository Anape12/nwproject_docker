package jp.nw.controller;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.nw.dao.ThreadDao;
import jp.nw.entity.UserEntity;

@WebServlet("/ThreadStatus")
public class ThreadStatusController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        UserEntity user = session == null ? null : (UserEntity) session.getAttribute("loginUser");
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (!Objects.equals(session.getAttribute("threadCsrfToken"), request.getParameter("csrfToken"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        int threadId;
        try {
            threadId = Integer.parseInt(request.getParameter("threadId"));
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String status = "reopen".equals(request.getParameter("action")) ? "OPEN" : "CLOSED";
        boolean updated = new ThreadDao().updateStatus(threadId, user.getUserId(),
                "1".equals(user.getPermission()), status);
        session.setAttribute("threadFlash", updated
                ? ("CLOSED".equals(status) ? "スレッドを完了しました。" : "スレッドを再開しました。")
                : "このスレッドを変更する権限がありません。");
        session.setAttribute("threadFlashType", updated ? "success" : "error");
        response.sendRedirect(request.getContextPath() + "/ThreadDetailController?id=" + threadId);
    }
}
