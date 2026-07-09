package jp.nw.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.nw.application.EditUserListViewCommand;
import jp.nw.base.BaseModel;
import jp.nw.entity.UserEntity;
import jp.nw.model.UserViewLogic;

/**
 * Servlet implementation class UserView
 */
@WebServlet("/UpdateUserInfo")
public class UpdateUserInfoController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Map<String, String> postMap;
	private BaseModel logger = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UpdateUserInfoController() {
		super();
		// TODO Auto-generated constructor stub
		this.logger = new BaseModel();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/plain;charset=UTF-8");
		PrintWriter out = response.getWriter();

		// **********************************************
		EditUserListViewCommand command = new EditUserListViewCommand();
		command.setCommandData(request, response);
		command.execute();
		// **********************************************
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html; charset=Shift_JIS");
		// 編集されたユーザ情報（ID,パスワード,権限レベル）を取得
		String nowID = (String) request.getParameter("nowID");
		String editID = (String) request.getParameter("editID");
		String editPassword = (String) request.getParameter("editPassword");
		String editPermission = (String) request.getParameter("editPermission");

		// ユーザー情報編集
		UserViewLogic userview = new UserViewLogic();
		List<UserEntity> userList = userview.confirUserInfo(nowID, editID, editPassword, editPermission);

		if (userList.size() == 0) {
			// エラー処理もしくはエラー画面を導入予定
			request.setAttribute("errorMsg", "更新対象のユーザーが存在しません。");
			RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/login/Error.jsp");
			// RequestDispatcher dispatcher =
			// request.getRequestDispatcher("/WEB-INF/jsp/otherUser/userList.jsp");
			dispatcher.forward(request, response);
		} else {
			request.setAttribute("userList", userList);

			HttpSession session = request.getSession();
			UserEntity loginUser = (UserEntity) session.getAttribute("loginUser");
			if (loginUser == null) {
				response.sendRedirect("/nwproject/");
			} else {
				RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/otherUser/userList.jsp");
				dispatcher.forward(request, response);
			}
		}
	}

}
