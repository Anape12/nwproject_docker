package jp.nw.controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.nw.entity.UserEntity;
import jp.nw.model.UserInsertLogic;

/**
 * Servlet implementation class UserUpdate
 */
@WebServlet("/UserInsertResult")
public class UserInsertResult extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UserInsertResult() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		UserEntity user = UserEntity.builder()
				.userId(request.getParameter("userId"))
				.password(request.getParameter("userPass"))
				.birthDate(request.getParameter("userBirth"))
				.permission(request.getParameter("userPermis"))
				.build();
		// 新規ユーザ－登録処理
		UserInsertLogic ul = new UserInsertLogic();
		boolean flg = ul.insertProcess(user);

		RequestDispatcher dispatcher = request.getRequestDispatcher("WEB-INF/jsp/RegUser/userInsertConf.jsp");
		dispatcher.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

	}

}
