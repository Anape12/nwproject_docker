package jp.nw.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import jp.nw.entity.UserEntity;
import jp.nw.parts.DBBase;
import jp.nw.parts.DaoPart;

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

		try {

			// ユーザー情報チェック処理
			if (!userInfoCheck(user)) {
				JFrame frame = new JFrame();
				JOptionPane.showMessageDialog(frame, "値を更新してください");
			} else {
				DBBase dbBase = new DBBase();
				con = dbBase.getConnection();
				StringBuilder sb = new StringBuilder();
				sb.append(DaoPart.SQL.UPDATE);
				sb.append(DaoPart.SQL.SPACE);
				sb.append("users");
				sb.append(DaoPart.SQL.SPACE);
				sb.append(DaoPart.SQL.SET);
				sb.append(DaoPart.SQL.SPACE);
				sb.append("password");
				sb.append(DaoPart.SQL.EQUARL);
				sb.append("?");
				sb.append(DaoPart.SQL.SPACE);
				sb.append(DaoPart.SQL.WHEHE);
				sb.append(DaoPart.SQL.SPACE);
				sb.append("name");
				sb.append(DaoPart.SQL.EQUARL);
				sb.append(DaoPart.SQL.SPACE);
				sb.append("?");

				ps = con.prepareStatement(sb.toString());

				ps.setString(1, user.getPassword());
				ps.setString(2, user.getUserId());

				con.setAutoCommit(false);
				try {
					// SQLカラム名
					// SQL設定値
					// Update処理実行
					dbCon = new DBBase();
					boolean resultFlg = dbCon.updateSQL(null, null, null);

					int result = ps.executeUpdate();
					System.out.println("結果" + result);
					con.commit();
				} catch (Exception e) {
					// ロールバック
					con.rollback();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// 各オブジェクトを解放する
			if (ps != null) {
				try {
					ps.close();
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
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
