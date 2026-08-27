package jp.nw.controller;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.nw.application.LoginCommand;
import jp.nw.base.BaseModel;
import jp.nw.base.CommandData;
import jp.nw.entity.UserEntity;
import jp.nw.util.SecurityToken;

/**
 * Servlet implementation class Login
 */
@WebServlet("/Login")
public class LoginController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private BaseModel baseModel = null;

	/**
	 * @see HttpServlet#
	 *
	 *
	 *      HttpServlet()
	 */
	public LoginController() {
		super();
		this.baseModel = new BaseModel();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/login/login.jsp");
		dispatcher.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");

		// コマンド処理の生成
		LoginCommand command = new LoginCommand();
		command.setCommandData(this.baseModel.getParameter(request, response));
		// コマンド処理の実行
		CommandData output = command.execute();
		// Outputよりユーザー情報を取得する
		UserEntity userEntity = (UserEntity) output.getValue("userobj");

		// ログイン処理失敗の場合、エラー画面へ
		if (userEntity.getPermission().equals("99")) {
			this.baseModel.writeInfo("ログイン失敗");
			// ログイン失敗
			RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/login/loginMiss.jsp");
			dispatcher.forward(request, response);
			return;
		}

		String token = UUID.randomUUID().toString();
		boolean isTokenUpdated = SecurityToken.updateToken(userEntity.getUserId(), token);

		// トークンの更新に失敗した場合もログイン不可
		if (!isTokenUpdated) {
			this.baseModel.writeInfo("トークン更新失敗");
			// ログイン失敗
			RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/login/loginMiss.jsp");
			dispatcher.forward(request, response);
			return;
		}

		// ログイン処理成功の場合、ユーザーID/トークンをセッションに保存
		HttpSession session = request.getSession();
		session.setAttribute("loginToken", token);
		session.setAttribute("loginUser", userEntity);

		this.baseModel.writeInfo(userEntity.getPermission().equals("1") ? "ログイン成功（管理者）" : "ログイン成功（一般）");
		response.sendRedirect(request.getContextPath() + "/MenuSelect");
	}
}
