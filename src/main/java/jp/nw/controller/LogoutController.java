package jp.nw.controller;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.nw.entity.UserEntity;
import jp.nw.model.AuditLogLogic;
import jp.nw.util.SecurityToken;

@WebServlet("/Logout")
public class LogoutController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("loginUser") == null) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }
        UserEntity loginUser = (UserEntity) session.getAttribute("loginUser");

        // LoginFilterで正規の画面からの要求であることを確認済みなので、全セッションを失効させる
        if (!SecurityToken.revokeAllSessions(loginUser.getUserId())) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "ログアウト処理に失敗しました。");
            return;
        }

        AuditLogLogic.record(request, "AUTH", "LOGOUT", "USER", loginUser.getUserId(), true, null);
        session.setAttribute("intentionalLogout", Boolean.TRUE);

        if (session != null) {
            session.invalidate();
        }

        Cookie sessionCookie = new Cookie("JSESSIONID", "");
        sessionCookie.setHttpOnly(true);
        sessionCookie.setMaxAge(0);
        sessionCookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        response.addCookie(sessionCookie);
        response.setHeader("Clear-Site-Data", "\"cache\"");

        response.sendRedirect(request.getContextPath() + "/Login");
    }
}
