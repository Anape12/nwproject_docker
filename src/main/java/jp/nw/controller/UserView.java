package jp.nw.controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.nw.application.UserListViewCommand;
import jp.nw.base.BaseModel;
import jp.nw.base.CommandData;

@WebServlet("/UserView")
public class UserView extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private BaseModel logger = null;

	private CommandData commandOutput;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UserView() {
		super();
		this.logger = new BaseModel();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// ユーザ情報取得処理
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
		response.sendRedirect(request.getContextPath() + "/UserSecurityAdmin");
	}

}
