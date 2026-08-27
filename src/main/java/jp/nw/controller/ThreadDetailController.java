package jp.nw.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.nw.dao.ThreadDao;
import jp.nw.dto.ThreadCommentDto;
import jp.nw.dto.ThreadDto;
import jp.nw.entity.UserEntity;

@WebServlet("/ThreadDetailController")
public class ThreadDetailController extends HttpServlet {

        private static final long serialVersionUID = 1L;

        protected void doGet(
                        HttpServletRequest request,
                        HttpServletResponse response)
                        throws ServletException, IOException {

                String idValue = request.getParameter("id");
                if (idValue == null) idValue = request.getParameter("threadId");
                int threadId;
                try {
                        threadId = Integer.parseInt(idValue);
                } catch (Exception e) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                        return;
                }

                ThreadDao dao = new ThreadDao();

                ThreadDto thread = dao.findById(threadId);
                if (thread == null) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                }

                List<ThreadCommentDto> commentList = dao.findComments(threadId);

                // ログインユーザーのセット
                HttpSession session = request.getSession();
                UserEntity loginUser = (UserEntity) session.getAttribute(
                                "loginUser");
                request.setAttribute(
                                "loginUserId",
                                loginUser.getUserId());
                request.setAttribute("canManageThread",
                                loginUser.getUserId().equals(thread.getAuthorId())
                                                || "1".equals(loginUser.getPermission()));
                if (session.getAttribute("threadCsrfToken") == null) {
                        session.setAttribute("threadCsrfToken", UUID.randomUUID().toString());
                }
                request.setAttribute("threadFlash", session.getAttribute("threadFlash"));
                request.setAttribute("threadFlashType", session.getAttribute("threadFlashType"));
                session.removeAttribute("threadFlash");
                session.removeAttribute("threadFlashType");

                request.setAttribute("thread", thread);
                request.setAttribute("commentList", commentList);

                RequestDispatcher dispatcher = request.getRequestDispatcher(
                                "/WEB-INF/jsp/thread/ThreadDetail.jsp");

                dispatcher.forward(request, response);
        }
}
