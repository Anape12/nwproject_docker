package jp.nw.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import jp.nw.entity.UserEntity;
import jp.nw.parts.DBBase;
import jp.nw.parts.Query;
import jp.nw.parts.SqlType;

public class UpdateLogic {
	PreparedStatement ps = null;
	Connection con = null;
	Map<Object, Object> param = new HashMap<>();

	// SQL発行
	private DBBase dbCon = null;

	// SQLカラム名情報
	private List<String> columnInfo = null;

	// SQL設定値情報
	private List<String> columnVal = null;

	public void execute(UserEntity user) {

		// ユーザー情報チェック処理
		if (!userInfoCheck(user)) {
			JFrame frame = new JFrame();
			JOptionPane.showMessageDialog(frame, "値を更新してください");
			return;
		}

		LinkedHashMap<String, Object> values = new LinkedHashMap<>();
		values.put("password", user.getPassword());

		LinkedHashMap<String, Object> conditions = new LinkedHashMap<>();
		conditions.put("user_id", user.getUserId());

		Query query = Query.builder()
				.sqlType(SqlType.UPDATE)
				.tableName("users_info")
				.values(values)
				.conditions(conditions)
				.build();

		DBBase db = new DBBase();

		int updateCount = (Integer) db.execute(query);

		if (updateCount == 0) {
			// 更新対象なし
		}
	}

	/**
	 * ユーザー情報更新チェック処理
	 * 
	 * @param user
	 * @return boolean
	 */
	private boolean userInfoCheck(UserEntity user) {
		String name = "";
		String pass = "";
		try {
			DBBase dbBase = new DBBase();
			con = dbBase.getConnection();
			String sql = "SELECT name, password FROM users WHERE name = ?";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, user.getUserId());

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				name = rs.getString("name");
				pass = rs.getString("password");
			}
			if (name.equals(user.getUserId())) {
				return false;
			}
			if (pass.equals(user.getPassword())) {
				return false;
			}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return true;
	}
}
