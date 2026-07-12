package jp.nw.filter;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.nw.entity.UserEntity;
import jp.nw.util.SecurityToken;

@WebFilter("/*")
public class LoginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();

        // 認証不要画面
        if (uri.endsWith("/Login")
                || uri.endsWith("/")
                || uri.contains("/css/")
                || uri.contains("/js/")
                || uri.contains("/images/")) {

            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);

        if (session == null) {
            res.sendRedirect(req.getContextPath() + "/Login");
            return;
        }

        if (session.getAttribute("loginUser") == null) {
            res.sendRedirect(req.getContextPath() + "/Login");
            return;
        }

        // トークン整合性チェック
        if (!integrityToken(req)) {
            session.invalidate();
            res.sendRedirect(req.getContextPath() + "/Login");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean integrityToken(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return false;
        }

        UserEntity loginUser = (UserEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return false;
        }

        String sessionToken = (String) session.getAttribute("loginToken");

        String currentToken = SecurityToken.getToken(loginUser.getUserId());

        return Objects.equals(sessionToken, currentToken);
    }
}