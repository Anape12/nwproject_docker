package jp.nw.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import jp.nw.entity.UserEntity;
import jp.nw.parts.DBBase;
import jp.nw.parts.PasswordUtil;
import jp.nw.parts.Query;

public class LoginLogic {

	// ログインパラメータ
	private Map<String, Object> param = null;
	// SQL発行オブジェクト
	private DBBase dbCon = null;
	// SQL結果格納Map
	private List<Map<String, Object>> resultList = null;

	// パスワード整合
	private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public Map<String, Object> execute(UserEntity userEntity, Query query) {

		try {

			// SQL SELECT共通部品実行
			dbCon = new DBBase();
			this.resultList = (List<Map<String, Object>>) dbCon.execute(query);

			// 取得結果Mapの取得
			param = new HashMap<>();

			// SQL実行結果を返却Mapへ格納(もっとスマートなやり方に追々修正)
			if (!this.resultList.isEmpty()) {
				for (String key : this.resultList.get(0).keySet()) {
					param.put(key, this.resultList.get(0).get(key));
				}
			}

			// パスワード整合性チェック
			if (loginCheck(userEntity, param)) {
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
	public boolean loginCheck(UserEntity userEntity, Map<String, Object> loginInfo) {
		if (userInfoCheck(loginInfo) && passwordCheck(userEntity, loginInfo) && passwordExpireCheck(loginInfo)) {
			return true;
		}

		return false;
	}

	// ユーザー情報の有無
	private boolean userInfoCheck(Map<String, Object> selectResultMap) {
		if (selectResultMap.get("user_id") != null) {
			return true;
		}

		return false;
	}

	// パスワードの整合性
	private boolean passwordCheck(UserEntity userEntity, Map<String, Object> selectResultMap) {
		if (PasswordUtil.matches(userEntity.getPassword(), (String) selectResultMap.get("password"))) {
			return true;
		}

		return false;
	}

	// パスワード有効期限
	private boolean passwordExpireCheck(Map<String, Object> selectResultMap) {
		// 現在日付を取得
		Calendar cl = Calendar.getInstance();

		// 日付をyyyyMMddの形で出力する
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String str = sdf.format(cl.getTime());

		if (Integer.parseInt(str) <= Integer.parseInt((String) selectResultMap.get("password_expiration"))) {
			return true;
		}

		return false;
	}
}
