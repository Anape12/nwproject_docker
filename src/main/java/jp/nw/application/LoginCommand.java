package jp.nw.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import jp.nw.base.ApplicationCommand;
import jp.nw.model.LoginLogic;
import jp.nw.model.User;
import jp.nw.parts.DaoPart;

public class LoginCommand extends ApplicationCommand {

	private String inputName = null;
	private String inputPass = null;
	private String permisson = null;
	private boolean loginChkF = false;
	private User user = null;

	private static final String KEY_USERID = "userId";
	private static final String KEY_USERPASS = "password";
	private static final String KEY_USERPERMISS = "permission";
	private static final String KEY_QERYNAME = "name";
	private static final String KEY_QERYRESULT = "result";
	private static final String KEY_USEROBJ = "userobj";

	public boolean setCommandData(Map<String, Object> loginParam) {

		try {
			this.inputName = (String) loginParam.get(KEY_USERID);
			// パスワードハッシュ化
			PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			this.inputPass = passwordEncoder.encode((String) loginParam.get(KEY_USERPASS));
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

			// SQL情報Map
			Map<String, Object> sqlInfo = new HashMap<String, Object>();
			// SELECT情報格納
			List<String> colum = new ArrayList<>();
			// WHERE情報格納
			List<String> sInfo = new ArrayList<>();
			// SELECT情報格納
			colum.add(KEY_USERPASS);
			colum.add(KEY_USERPERMISS);
			sqlInfo.put(DaoPart.KOMOKU_INFO.SELECT_INFO, colum);
			// WHERE情報格納
			sInfo.add(KEY_QERYNAME);
			sqlInfo.put(DaoPart.KOMOKU_INFO.WHERE_INFO, sInfo);
			// Table Name
			String table = "users_info";

			LoginLogic loginLogic = new LoginLogic();
			Map<String, Object> isLogin = loginLogic.execute(user, sqlInfo, table);

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
