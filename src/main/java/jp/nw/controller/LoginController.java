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

import jp.nw.base.BaseModel;
import jp.nw.entity.UserEntity;
import jp.nw.model.AuthenticationLogic;
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

		String userId = request.getParameter("userId");
		AuthenticationLogic.Result result = new AuthenticationLogic().authenticate(
				userId == null ? "" : userId.trim(), request.getParameter("password"), request);
		if (!result.authenticated()) {
			this.baseModel.writeInfo("ログイン失敗");
			request.setAttribute("errorMessage", result.message());
			RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/login/loginMiss.jsp");
			dispatcher.forward(request, response);
			return;
		}
		UserEntity userEntity = result.user();

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
		HttpSession oldSession = request.getSession(false);
		if (oldSession != null)
			oldSession.invalidate();
		HttpSession session = request.getSession(true);
		session.setMaxInactiveInterval(sessionTimeoutSeconds());
		session.setAttribute("loginToken", token);
		session.setAttribute("loginUser", userEntity);
		session.setAttribute("forcePasswordChange", result.forcePasswordChange());

		this.baseModel.writeInfo(userEntity.getPermission().equals("1") ? "ログイン成功（管理者）" : "ログイン成功（一般）");
		response.sendRedirect(
				request.getContextPath() + (result.forcePasswordChange() ? "/ChangePassword" : "/MenuSelect"));
	}

	private int sessionTimeoutSeconds() {
		try {
			int minutes = Integer.parseInt(System.getenv("SESSION_TIMEOUT_MINUTES"));
			return Math.max(minutes, 1) * 60;
		} catch (Exception e) {
			return 30 * 60;
		}
	}
}
