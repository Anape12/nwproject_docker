package jp.nw.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.nw.base.ApplicationCommand;
import jp.nw.model.LoginLogic;
import jp.nw.model.User;
import jp.nw.parts.Query;
import jp.nw.parts.SqlType;

public class LoginCommand extends ApplicationCommand {

	private String inputName = null;
	private String inputPass = null;
	private String permisson = null;
	private boolean loginChkF = false;
	private User user = null;

	private static final String KEY_USERID = "userId";
	private static final String KEY_USERPASS = "password";
	private static final String KEY_USERPERMISS = "permission";
	private static final String KEY_PASS_EXPIRATION = "password_expiration";
	private static final String KEY_QERYNAME = "user_id";
	private static final String KEY_QERYRESULT = "result";
	private static final String KEY_USEROBJ = "userobj";

	public boolean setCommandData(Map<String, Object> loginParam) {

		try {
			this.inputName = (String) loginParam.get(KEY_USERID);
			this.inputPass = (String) loginParam.get(KEY_USERPASS);
		} catch (Exception e) {
			e.toString();
			return false;
		}
		return true;

	}

	public boolean doCommandData() {

		try {

			// ID/password取得クラス
			this.user = new User(this.inputName, this.inputPass);

			// WHERE情報格納
			List<String> sInfo = new ArrayList<>();
			sInfo.add(KEY_QERYNAME);

			// Query情報格納
			Query query = Query.builder()
                    .sqlType(SqlType.SELECT)
                    .tableName("users_info")
                    .selectColumns(List.of(KEY_QERYNAME, KEY_USERPASS, KEY_USERPERMISS, KEY_PASS_EXPIRATION))
					.conditions(Map.of(KEY_QERYNAME, user.getName()))
                    .build();

			LoginLogic loginLogic = new LoginLogic();
			Map<String, Object> isLogin = loginLogic.execute(user, query);

			// ユーザー情報の有無チェック
			if (!loginLogic.loginCheck(user, isLogin)) {
				this.logger.writeInfo("ユーザー情報が存在しません。");
				return false;
			}

			// SQL実行結果を取得
			this.loginChkF = (boolean) isLogin.get(KEY_QERYRESULT);
			// 権限レベルを取得
			this.permisson = (String) isLogin.get(KEY_USERPERMISS);

			return true;
		} catch (Exception e) {
			this.logger.writeInfo("SQL Error");
			return false;
		}

	}

	public boolean executeCommand() {

		try {

			// ユーザー情報の有無

			// パスワードの整合性

			// パスワード有効期限

			// これらを以ってログイン成功とする

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
		this.output.setValue(KEY_USEROBJ, this.user);

		return true;
	}
}
