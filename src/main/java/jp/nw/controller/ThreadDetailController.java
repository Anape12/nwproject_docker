package jp.nw.controller;

import java.io.IOException;
import java.util.List;

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

                int threadId = Integer.parseInt(request.getParameter("id"));

                ThreadDao dao = new ThreadDao();

                ThreadDto thread = dao.findById(threadId);

                List<ThreadCommentDto> commentList = dao.findComments(threadId);

                // ログインユーザーのセット
                HttpSession session = request.getSession();
                UserEntity loginUser = (UserEntity) session.getAttribute(
                                "loginUser");
                request.setAttribute(
                                "loginUserId",
                                loginUser.getUserId());

                request.setAttribute("thread", thread);
                request.setAttribute("commentList", commentList);

                RequestDispatcher dispatcher = request.getRequestDispatcher(
                                "/WEB-INF/jsp/thread/ThreadDetail.jsp");

                dispatcher.forward(request, response);
        }
}