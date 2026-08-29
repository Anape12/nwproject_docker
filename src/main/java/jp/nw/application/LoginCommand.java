package jp.nw.application;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jp.nw.base.ApplicationCommand;
import jp.nw.entity.UserEntity;
import jp.nw.model.LoginLogic;
import jp.nw.parts.Query;
import jp.nw.parts.SqlType;

public class LoginCommand extends ApplicationCommand {

	private boolean loginChkF = false;
	private UserEntity userEntity = null;

	private static final String KEY_USERID = "userId";
	private static final String KEY_USERPASS = "password";
	private static final String KEY_USERPERMISS = "permission";
	private static final String KEY_PASS_EXPIRATION = "password_expiration";
	private static final String KEY_FIRST_NAME = "first_name";
	private static final String KEY_LAST_NAME = "last_name";
	private static final String KEY_ACCOUNT_TYPE = "account_type";
	private static final String KEY_QERYNAME = "user_id";
	private static final String KEY_USEROBJ = "userobj";

	public boolean setCommandData(Map<String, Object> loginParam) {

		try {
			this.userEntity = UserEntity.builder()
					.userId((String) loginParam.get(KEY_USERID))
					.password((String) loginParam.get(KEY_USERPASS))
					.build();
		} catch (Exception e) {
			e.toString();
			return false;
		}
		return true;

	}

	public boolean doCommandData() {

		try {
			// WHERE情報格納
			List<String> sInfo = new ArrayList<>();
			sInfo.add(KEY_QERYNAME);

			LinkedHashMap<String, Object> conditions = new LinkedHashMap<>();
			conditions.put(KEY_QERYNAME, this.userEntity.getUserId());

			// Query情報格納
			Query query = Query.builder()
					.sqlType(SqlType.SELECT)
					.tableName("users_info")
					.selectColumns(List.of(KEY_QERYNAME, KEY_USERPASS, KEY_USERPERMISS, KEY_PASS_EXPIRATION, KEY_FIRST_NAME, KEY_LAST_NAME, KEY_ACCOUNT_TYPE))
					.conditions(conditions)
					.build();

			LoginLogic loginLogic = new LoginLogic();
			List<UserEntity> userResults = loginLogic.execute(userEntity, query);

			// ユーザー情報がなかった場合
			if (userResults.isEmpty()) {
				this.logger.writeInfo("ユーザー情報が存在しません。");
				return false;
			}

			// SQL実行結果を取得
			this.loginChkF = (boolean) userResults.isEmpty() ? false : true;
			// 権限レベルを取得
			// this.permisson = (String) userResults.get(0).get(KEY_USERPERMISS);
			this.userEntity.setPermission((String) userResults.get(0).getPermission());
			this.userEntity.setFirstName((String) userResults.get(0).getFirstName());
			this.userEntity.setLastName((String) userResults.get(0).getLastName());

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
		this.output.setValue(KEY_USEROBJ, this.userEntity);
		return true;
	}
}
