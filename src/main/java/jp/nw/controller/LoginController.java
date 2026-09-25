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
import jp.nw.util.StatusCheckUtil;

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
		if ("1".equals(request.getParameter("sessionInvalid"))) {
			request.setAttribute("errorMessage", "別のブラウザまたは端末でログインされたため、以前のセッションを終了しました。再度ログインしてください。");
		} else if ("1".equals(request.getParameter("windowInvalid"))) {
			request.setAttribute("errorMessage", "この画面を開いた後に同じブラウザで再ログインされたため、この画面からの操作を停止しました。再度ログインしてください。");
		}
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

		// 現在のブラウザに既存セッションがあれば、先に破棄する
		HttpSession oldSession = request.getSession(false);
		if (oldSession != null) {
			oldSession.setAttribute("intentionalLogout", Boolean.TRUE);
			oldSession.invalidate();
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
		HttpSession session = request.getSession(true);
		session.setMaxInactiveInterval(sessionTimeoutSeconds());
		session.setAttribute("loginToken", token);
		session.setAttribute("loginContext", UUID.randomUUID().toString());
		session.setAttribute("loginUser", userEntity);
		session.setAttribute("forcePasswordChange", result.forcePasswordChange());

		this.baseModel
				.writeInfo(StatusCheckUtil.isAdministrator(userEntity.getPermission()) ? "ログイン成功（管理者）" : "ログイン成功（一般）");
		response.sendRedirect(
				request.getContextPath()
						+ (result.forcePasswordChange() ? "/ChangePassword?loginFresh=1" : "/MenuSelect?loginFresh=1"));
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
