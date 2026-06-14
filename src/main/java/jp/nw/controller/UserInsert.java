package jp.nw.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.nw.base.BaseAppLogic;
import jp.nw.entity.UserEntity;

/**
 * Servlet implementation class UserUpdate
 */
@WebServlet("/UserInsert")
public class UserInsert extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private BaseAppLogic baseApplogic = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UserInsert() {
		super();
		baseApplogic = new BaseAppLogic();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		RequestDispatcher dispatcher = request.getRequestDispatcher("WEB-INF/jsp/RegUser/userInsert.jsp");
		dispatcher.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		request.setCharacterEncoding("UTF-8");

		Map<String, Object> userMap = baseApplogic.getParameter(request, response);
		System.out.println(userMap);

		String userId = (String) userMap.get("userid");
		String userPass = (String) userMap.get("userpass");
		String year = (String) userMap.get("birthYear");
		String month = (String) userMap.get("birthMonth");
		String date = (String) userMap.get("birthDate");
		// 生年月日を結合
		StringBuilder sb = new StringBuilder();
		sb.append(year);
		sb.append("-");
		sb.append(month);
		sb.append("-");
		sb.append(date);

		UserEntity user = UserEntity.builder()
				.userId(userId)
				.password(userPass)
				.birthDate(sb.toString())
				.permission((String) userMap.get("userpermis"))
				.build();

		HttpSession session = request.getSession();
		session.setAttribute("loginUser", user);
		RequestDispatcher dispatcher = request.getRequestDispatcher("WEB-INF/jsp/RegUser/userInsertCheck.jsp");
		dispatcher.forward(request, response);

	}

}
