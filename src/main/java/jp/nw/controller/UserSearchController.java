package jp.nw.controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.nw.application.UserListViewCommand;
import jp.nw.application.UserSearchCommand;
import jp.nw.base.BaseModel;
import jp.nw.base.CommandData;

@WebServlet("/UserSearch")
public class UserSearchController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private BaseModel logger = null;

	private CommandData commandOutput;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UserSearchController() {
		super();
		this.logger = new BaseModel();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String action = request.getParameter("action");

		// 編集画面起動
		if ("edit".equals(action)) {
			String userId = request.getParameter("userId");
			request.setAttribute("userId", userId);
			// ユーザ情報取得処理
			UserListViewCommand command = new UserListViewCommand();
			command.setCommandData(request, response);

			// 処理を実行
			CommandData output = command.postExec();

			RequestDispatcher dispatcher = ((HttpServletRequest) output.getValue("request"))
					.getRequestDispatcher("/WEB-INF/jsp/otherUser/editUserInfo.jsp");
			dispatcher.forward(((HttpServletRequest) output.getValue("request")),
					((HttpServletResponse) output.getValue("response")));
		} else {
			// ユーザー情報検索画面起動
			RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/otherUser/userSearch.jsp");
			dispatcher.forward(request, response);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 入力テキストを取得
		request.setCharacterEncoding("UTF-8");
		String userId = request.getParameter("userId");

		if (userId == null || userId.trim().isEmpty()) {
			// 検索テキストが空の場合は、全件検索を行う
			UserListViewCommand command = new UserListViewCommand();
			command.setCommandData(request, response);
			// 処理を実行
			CommandData output = command.execute();
			if (output.getValue("loginUser") == null) {
				((HttpServletResponse) output.getValue("response")).sendRedirect("/nwproject/");
			} else {
				RequestDispatcher dispatcher = ((HttpServletRequest) output.getValue("request"))
						.getRequestDispatcher("/WEB-INF/jsp/otherUser/userSearch.jsp");
				dispatcher.forward(request, response);
			}
		} else {
			// ユーザーIDで検索を行う
			UserSearchCommand command = new UserSearchCommand();
			command.setCommandData(request, response);

			CommandData output = command.execute();

			if (output.getValue("loginUser") == null) {
				((HttpServletResponse) output.getValue("response")).sendRedirect("/nwproject/");
			} else {
				RequestDispatcher dispatcher = ((HttpServletRequest) output.getValue("request"))
						.getRequestDispatcher("/WEB-INF/jsp/otherUser/userSearch.jsp");
				dispatcher.forward(request, response);
			}
		}

	}

}
