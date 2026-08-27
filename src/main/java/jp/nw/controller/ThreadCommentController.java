package jp.nw.controller;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import jp.nw.dao.ThreadDao;
import jp.nw.dto.ThreadCommentDto;
import jp.nw.dto.ThreadDto;
import jp.nw.entity.UserEntity;

@WebServlet("/ThreadCommentController")
public class ThreadCommentController
                extends HttpServlet {

        protected void doPost(
                        HttpServletRequest request,
                        HttpServletResponse response)
                        throws ServletException, IOException {

                request.setCharacterEncoding("UTF-8");

                int threadId = Integer.parseInt(
                                request.getParameter(
                                                "threadId"));

                String commentText = request.getParameter("commentText");
                commentText = commentText == null ? "" : commentText.trim();

                HttpSession session = request.getSession(false);
                UserEntity loginUser = session == null ? null
                                : (UserEntity) session.getAttribute("loginUser");
                if (loginUser == null) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                }
                if (!java.util.Objects.equals(session.getAttribute("threadCsrfToken"),
                                request.getParameter("csrfToken"))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                }
                if (commentText.isEmpty() || commentText.length() > 500) {
                        flash(session, "コメントは1～500文字で入力してください。", "error");
                        response.sendRedirect(request.getContextPath() + "/ThreadDetailController?id=" + threadId);
                        return;
                }

                ThreadCommentDto dto = new ThreadCommentDto();

                dto.setThreadId(threadId);

                dto.setCommentText(commentText);

                dto.setAuthorId(loginUser.getUserId());

                ThreadDao dao = new ThreadDao();

                ThreadDto thread = dao.findById(threadId);
                if (thread == null) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                }
                if (thread.isClosed()) {
                        flash(session, "完了済みのスレッドには投稿できません。", "error");
                } else if (dao.insertComment(dto)) {
                        flash(session, "コメントを投稿しました。", "success");
                }

                response.sendRedirect(
                                request.getContextPath() + "/ThreadDetailController?id="
                                                + threadId);
        }

        private void flash(HttpSession session, String message, String type) {
                session.setAttribute("threadFlash", message);
                session.setAttribute("threadFlashType", type);
        }
}
