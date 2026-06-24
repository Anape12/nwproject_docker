package jp.nw.controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.nw.application.InitializeUserCommand;
import jp.nw.application.UserListViewCommand;
import jp.nw.base.BaseModel;
import jp.nw.base.CommandData;

/**
 * Servlet implementation class UserView
 */
@WebServlet("/UserView")
public class UserView extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private BaseModel logger = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UserView() {
		super();
		this.logger = new BaseModel();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// ユーザ所法取得処理
		UserListViewCommand command = new UserListViewCommand();
		command.setCommandData(request, response);
		// 処理を実行
		CommandData output = command.execute();

		if (output.getValue("loginUser") == null) {
			((HttpServletResponse) output.getValue("response")).sendRedirect("/nwproject/");
		} else {
			RequestDispatcher dispatcher = ((HttpServletRequest) output.getValue("request"))
					.getRequestDispatcher("/WEB-INF/jsp/otherUser/userList.jsp");
			dispatcher.forward(request, response);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 実行タスクの取得
		String action = request.getParameter("action");
		switch (action) {
			// 変更処理
			case "change":
				// ユーザ情報取得処理
				UserListViewCommand command = new UserListViewCommand();
				command.setCommandData(request, response);

				// 処理を実行
				CommandData output = command.postExec();

				if (output.getValue("loginUser") == null) {
					((HttpServletResponse) output.getValue("response")).sendRedirect("/nwproject/");
					((HttpServletRequest) output.getValue("request")).setCharacterEncoding("UTF-8");
					((HttpServletRequest) output.getValue("request")).setAttribute("errorMsg", "エラー");
				} else {
					RequestDispatcher dispatcher = ((HttpServletRequest) output.getValue("request"))
							.getRequestDispatcher("/WEB-INF/jsp/otherUser/editUserInfo.jsp");
					dispatcher.forward(((HttpServletRequest) output.getValue("request")),
							((HttpServletResponse) output.getValue("response")));
				}
				break;

			// 初期化処理
			case "reset":
				InitializeUserCommand initializeCmd = new InitializeUserCommand();
				initializeCmd.setCommandData(request, response);
				initializeCmd.execute();
				break;
		}
		// // ユーザ情報取得処理
		// UserListViewCommand command = new UserListViewCommand();
		// command.setCommandData(request, response);
		// // 処理を実行
		// CommandData output = command.postExec();

		// if (output.getValue("loginUser") == null) {
		// ((HttpServletResponse)
		// output.getValue("response")).sendRedirect("/nwproject/");
		// ((HttpServletRequest)
		// output.getValue("request")).setCharacterEncoding("UTF-8");
		// ((HttpServletRequest) output.getValue("request")).setAttribute("errorMsg",
		// "エラー");
		// } else {
		// RequestDispatcher dispatcher = ((HttpServletRequest)
		// output.getValue("request"))
		// .getRequestDispatcher("/WEB-INF/jsp/otherUser/editUserInfo.jsp");
		// dispatcher.forward(((HttpServletRequest) output.getValue("request")),
		// ((HttpServletResponse) output.getValue("response")));
		// }
	}

}
