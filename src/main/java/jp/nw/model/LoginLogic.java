package jp.nw.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import jp.nw.parts.DBBase;
import jp.nw.parts.Query;

public class LoginLogic {

	// ログインパラメータ
	private Map<String, Object> param = null;
	// SQL発行オブジェクト
	private DBBase dbCon = null;
	// SQL結果格納Map
	private Map<String, Object> selectResultMap = null;

	// パスワード整合
	private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public Map<String, Object> execute(User user, Query query) {

		try {

			// SQL SELECT共通部品実行
			dbCon = new DBBase();
			this.selectResultMap = dbCon.userInfoSql(query);

			// SQL実行結果を返却Mapへ格納(もっとスマートなやり方に追々修正)
			for (String key : this.selectResultMap.keySet()) {
				param.put(key, this.selectResultMap.get(key));
			}

			// パスワード整合性チェック
			if (this.passwordEncoder.matches(user.getPass(), (String) selectResultMap.get("password"))) {
				param.put("result", true);
				return param;
			} else {
				param.put("result", false);
				return param;
			}
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return param;
	}

	// 対象ユーザーのログイン要件を一括チェック
	public boolean loginCheck(User user, Map<String, Object> loginInfo) {
		boolean chkResult = false;

		if (userInfoCheck(loginInfo) && passwordCheck(user, loginInfo) && passwordExpireCheck(loginInfo)) {
			chkResult = true;
		} else {
			chkResult = false;
		}

		return chkResult;
	}

	// ユーザー情報の有無
	private boolean userInfoCheck(Map<String, Object> selectResultMap) {
		if (selectResultMap.get("userid") != null) {
			return true;
		} else {
			return false;
		}
	}

	// パスワードの整合性
	private boolean passwordCheck(User user, Map<String, Object> selectResultMap) {
		if (this.passwordEncoder.matches(user.getPass(), (String) selectResultMap.get("password"))) {
			return true;
		} else {
			return false;
		}
	}
	// パスワード有効期限
	private boolean passwordExpireCheck(Map<String, Object> selectResultMap) { 
		// 現在日付を取得
		Calendar cl = Calendar.getInstance();

		//日付をyyyyMMddの形で出力する
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String str = sdf.format(cl.getTime());

		if (Integer.parseInt(str) <= Integer.parseInt((String) selectResultMap.get("password_expiration"))) {
			return true;
		} else {
			return false;
		}
	}
}
