package jp.nw.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

	public List<UserEntity> execute(UserEntity userEntity, Query query) {

		List<UserEntity> userList = new ArrayList<>();

		try {

			// SQL SELECT共通部品実行
			dbCon = new DBBase();
			userList = (List<UserEntity>)dbCon.execute(query, UserEntity.class);

			if(userList.isEmpty()) {
				return userList;
			}

			if(userList.size() > 1) {
				// 複数件ヒットはありえないので、エラーとして扱う
				return new ArrayList<>();
			}

			// パスワード整合性チェック
			if (loginCheck(userEntity, userList.get(0))) {
				return userList;
			} else {
				return new ArrayList<>();
			}
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return userList;
	}

	// 対象ユーザーのログイン要件を一括チェック
	public boolean loginCheck(UserEntity userEntity, UserEntity userList) {
		if (userList == null) {
			return false;
		}
		if (userInfoCheck(userList.getUserId()) && passwordCheck(userEntity, userList.getPassword()) && passwordExpireCheck(userList.getPasswordExpiration())) {
			return true;
		}

		return false;
	}

	// ユーザー情報の有無
	private boolean userInfoCheck(String userid) {
		if (userid != null && !userid.isEmpty()) {
			return true;
		}

		return false;
	}

	// パスワードの整合性
	private boolean passwordCheck(UserEntity userEntity, String password) {
		if (PasswordUtil.matches(userEntity.getPassword(), password)) {
			return true;
		}

		return false;
	}

	// パスワード有効期限
	private boolean passwordExpireCheck(String passwordExpiration) {
		// 現在日付を取得
		Calendar cl = Calendar.getInstance();

		// 日付をyyyyMMddの形で出力する
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String str = sdf.format(cl.getTime());

		if (Integer.parseInt(str) <= Integer.parseInt(passwordExpiration)) {
			return true;
		}

		return false;
	}
}
