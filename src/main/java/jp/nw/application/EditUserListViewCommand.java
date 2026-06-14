package jp.nw.application;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.nw.base.ApplicationCommand;
import jp.nw.entity.UserEntity;
import jp.nw.model.UserViewLogic;

public class EditUserListViewCommand extends ApplicationCommand {

	private String inputName = null;
	private String inputPass = null;
	private String permisson = null;
	private boolean loginChkF = false;
	private UserEntity userEntity = null;

	private Map<String, String> reqMap = null;
	private HttpServletRequest request = null;
	private HttpServletResponse response = null;

	private static final String KEY_USERID = "userId";
	private static final String KEY_USERPASS = "password";
	private static final String KEY_USERPERMISS = "permission";
	private static final String KEY_QERYNAME = "name";
	private static final String KEY_QERYRESULT = "result";
	private static final String KEY_USEROBJ = "userobj";

	public boolean setCommandData(HttpServletRequest request, HttpServletResponse response) {

		this.request = request;
		this.response = response;

		// 受け渡しパラメータの取得
		String param = this.request.getQueryString();
		// パラメータ区切り処理
		String[] params = param.split("&");
		this.reqMap = new HashMap<String, String>();
		String[] parameter = new String[2];
		for (int i = 0; i < params.length; i++) {
			String check = params[i];
			parameter = check.split("=");
			this.reqMap.put(parameter[0], parameter[1]);
		}
		return true;

	}

	public boolean doCommandData() {

		try {

			PrintWriter out = response.getWriter();

			// グローバルMapにユーザー情報保持
			// postMap = new HashMap<>(this.reqMap);
			// 各ユーザー情報の取得
			String userId = this.reqMap.get("userId");
			String userpass = this.reqMap.get("userPass");
			String userperm = this.reqMap.get("userPerm");
			UserViewLogic userInfo = new UserViewLogic();
			boolean userFlg = userInfo.userInfoCheck(userId, userpass, userperm);
			if (!userFlg) {
				out.print("0");
			} else {
				out.print("1");
				UserViewLogic userview = new UserViewLogic();
				boolean checkAfter = userview.userInfoUpdate(this.reqMap);
				if (checkAfter) {
					this.logger.writeInfo("ユーザー情報更新成功");
					RequestDispatcher dispatcher = this.request
							.getRequestDispatcher("/WEB-INF/jsp/otherUser/result.jsp");
					dispatcher.forward(this.request, this.response);
				} else {
					RequestDispatcher dispatcher = this.request.getRequestDispatcher("/WEB-INF/jsp/Error.jsp");
					dispatcher.forward(this.request, this.response);
				}
			}
			return true;

		} catch (Exception e) {
			this.logger.writeInfo("SQL Error");
			return false;
		}

	}

	public boolean executeCommand() {

		try {

			// ログイン後の遷移先画面を選択
			if (this.loginChkF) {
				/**
				 * ログインPassの入力回数をチェックし
				 * ３回以上失敗の場合はアカウントロック
				 */
			} else {
				this.output.setValue(KEY_USERPERMISS, "99");
			}

			return true;

		} catch (Exception e) {
			this.logger.writeInfo("");
			return false;
		}
	}

	public boolean commandOutput() {

		this.output.setValue(KEY_USERPERMISS, this.permisson);
		this.output.setValue(KEY_USEROBJ, this.userEntity);

		return true;
	}
}
