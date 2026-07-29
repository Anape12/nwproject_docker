package jp.nw.controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.nw.entity.UserEntity;

/**
 * Servlet implementation class UserView
 */
@WebServlet("/StartChat")
public class StartChatController extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		RequestDispatcher dispatcher = request.getRequestDispatcher(
				"/WEB-INF/jsp/chat/ChatWindow.jsp");

		dispatcher.forward(request, response);

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		String comment = request.getParameter("commentText");

		HttpSession session = request.getSession();
		UserEntity userEntity = (UserEntity)session.getAttribute("loginUser");

		System.out.println(comment);
		System.out.println(userEntity);
	}

}
