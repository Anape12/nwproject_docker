package jp.nw.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jp.nw.base.BaseModel;
import jp.nw.entity.UserEntity;
import jp.nw.parts.DBBase;
import jp.nw.parts.PasswordUtil;
import jp.nw.parts.Query;
import jp.nw.parts.SqlType;

public class UserViewLogic extends BaseModel {

	// SQL発行オブジェクト
	private DBBase dbCon = null;

	// 返却情報格納List
	private List<List<String>> retList = null;

	// ユーザー名
	private static String USER_NAME = "name";

	// ユーザーパスワード
	private static String USER_PASS = "password";

	// ユーザー権限
	private static String USER_PERMS = "permission";

	/**
	 * ユーザ―情報一覧表示
	 */
	public List<UserEntity> findAll() {
		List<UserEntity> userList = new ArrayList<>();

		BaseModel.logger.writeInfo("UsreViewLogic-findAll");

		// Connection取得
		dbCon = new DBBase();

		try (Connection conn = dbCon.getConnection()) {

			// テーブル名
			String trgTable = "users_info";
			// カラム情報
			List<String> colList = new ArrayList<String>();
			colList.add("id");
			colList.add("user_id");
			colList.add("password");
			colList.add("permission");
			// 検索条件
			Map<String, String> whereInfo = new HashMap<String, String>();
			whereInfo.put("delete_flg", "0");
			whereInfo.put("PROCESS_INFO", "ORDER BY id");

			LinkedHashMap<String, Object> conditions = new LinkedHashMap<>();
			conditions.put("delete_flg", "0");

			Map<String, List<String>> querySub = new HashMap<>();
			querySub.put("PROCESS_INFO", Arrays.asList("ORDER BY id"));

			retList = new ArrayList<List<String>>();

			// 要動作チェック(DBでint型のカラムは失敗するはず)
			Query query = Query.builder()
					.sqlType(SqlType.SELECT)
					.tableName(trgTable)
					.selectColumns(colList)
					.conditions(conditions)
					.querySub(querySub)
					.build();

			List<Map<String, Object>> result = (List<Map<String, Object>>) dbCon.execute(query);

			for (Map<String, Object> rowInfo : result) {
				int id = (int) rowInfo.get("id");
				String name = (String) rowInfo.get("user_id");
				String pass = (String) rowInfo.get("password");
				String permission = (String) rowInfo.get("permission");
				UserEntity userEntity = UserEntity.builder()
						.id(id)
						.userId(name)
						.password(pass)
						.permission(permission)
						.build();
				userList.add(userEntity);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return userList;
	}

	/**
	 * 編集ユーザ－情報取得
	 */
	public List<UserEntity> editUserInfo(String userId) {
		List<UserEntity> userList = new ArrayList<>();

		// Connection取得
		dbCon = new DBBase();

		try (Connection conn = dbCon.getConnection()) {
			String sql = "SELECT user_id,password,permission FROM users_info where user_id =? ORDER BY id";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				String id = rs.getString("user_id");
				String pass = rs.getString("password");
				String permission = rs.getString("permission");
				UserEntity userEntity = UserEntity.builder()
						.userId(id)
						.password(pass)
						.permission(permission)
						.build();
				userList.add(userEntity);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return userList;
	}

	/**
	 * ユーザー情報変更確定処理
	 */
	public List<UserEntity> confirUserInfo(String nowId, String userId, String userPass, String userPermission) {
		List<UserEntity> userList = new ArrayList<>();

		// Connection取得
		dbCon = new DBBase();

		try (Connection conn = dbCon.getConnection()) {
			// ユーザー情報編集チェック処理
			if (!userInfoCheck(userId, userPass, userPermission)) {
				// JFrame frame = new JFrame();
				// JOptionPane.showMessageDialog(frame, "値を更新してください");
			} else {
				String sql = "UPDATE users_info Set user_id = ?, password = ?, permission = ? where user_id=?";
				PreparedStatement ps = conn.prepareStatement(sql);
				int permission = Integer.parseInt(userPermission);
				ps.setString(1, userId);
				ps.setString(2, PasswordUtil.encode(userPass));
				ps.setInt(3, permission);
				ps.setString(4, nowId);
				// 更新処理
				int num = ps.executeUpdate();
				if (num == 0) {
					userList = findAll();
					return userList;
				} else {
					userList = findAll();
					return userList;
				}
			}
			return userList;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ユーザー情報更新チェック処理
	 * 
	 * @param user
	 * @return boolean
	 */
	public boolean userInfoCheck(String nowId, String nowPass, String permisstion) {
		String name = "";
		String pass = "";
		int permiss = 2;
		// Connection取得
		dbCon = new DBBase();

		try {
			Connection con = dbCon.getConnection();
			String sql = "SELECT user_id, password, permission FROM users_info WHERE user_id = ?";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, nowId);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				name = rs.getString("user_id");
				pass = rs.getString("password");
				permiss = rs.getInt("permission");
			}
			Integer i = Integer.valueOf(permiss);
			String perm = i.toString();

			if (name.equals(nowId) && pass.equals(nowPass) && permisstion.equals(perm)) {
				return false;
			}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return true;
	}

	/**
	 *
	 */
	public boolean userInfoUpdate(Map<String, String> postMap) {
		// 編集されたユーザ情報（ID,パスワード,権限レベル）を取得
		String nowUserId = postMap.get("nowId");
		String userId = postMap.get("userId");
		String userPass = postMap.get("userPass");
		String userPermission = postMap.get("userPerm");
		// ユーザー情報編集
		UserViewLogic userview = new UserViewLogic();
		List<UserEntity> userList = userview.confirUserInfo(nowUserId, userId, userPass, userPermission);

		if (userList.size() == 0) {
			// エラー
			return false;
		} else {
			return true;
		}

	}
}