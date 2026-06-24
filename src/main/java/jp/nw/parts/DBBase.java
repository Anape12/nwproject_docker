package jp.nw.parts;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBBase {

	String host = System.getenv("DB_HOST");
	String db = System.getenv("DB_NAME");
	String URL = "jdbc:mysql://" + host + ":3306/" + db
			+ "?serverTimezone=Asia/Tokyo&allowPublicKeyRetrieval=true&useSSL=false";
	private final String USER = "root";
	private final String PASSWORD = "root";

	private Connection con = null;
	// SELECT情報一時退避領域
	private List<String> selectInfo = null;
	// 発行SQL
	private StringBuilder sb = null;
	// SQL結果返却用List
	private List<Map<String, Object>> resultList = null;
	// 複数項目返却Map
	private Map<String, Map<String, Object>> dupliMap = null;
	// 複数項目返却MapKey
	private String deuliKey = null;

	public DBBase() {
		try {
			// Connection生成
			Class.forName("com.mysql.cj.jdbc.Driver");
			this.con = DriverManager.getConnection(URL, USER, PASSWORD);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Connection getConnection() {
		return this.con;
	}

	public Object execute(Query query) {
		switch (query.getSqlType()) {
			case SELECT:
				return executeSelect(query);

			case INSERT:
				return executeInsert(query);

			case UPDATE:
				return executeUpdate(query);

			case DELETE:
				return executeDelete(query);

			default:
				throw new IllegalArgumentException("Unsupported SQL type: " + query.getSqlType());
		}
	}

	private Object executeSelect(Query query) {
		try {
			// Map初期化
			resultList = new ArrayList<Map<String, Object>>();
			dupliMap = new HashMap<String, Map<String, Object>>();

			// 一時退避領域へ各パラメータを格納
			this.selectInfo = query.getSelectColumns();

			// SQL発行
			sb = new StringBuilder();
			sb.append(DaoPart.SQL.SELECT);
			sb.append(DaoPart.SQL.SPACE);

			// 取得情報の構築
			sb.append(createColumnInfo(this.selectInfo));
			// テーブル情報の設定
			sb.append(createFromInfo(query.getTableName()));
			// 条件句の構築
			sb.append(createWhereInfo(query.getConditions()));
			// 結果加工条件の構築
			sb.append(createQuerySubString(query.getQuerySub()));

			// バインド変数定義
			PreparedStatement ps = con.prepareStatement(sb.toString());

			// バインド変数設定
			int cnt = 1;
			for (Object value : query.getConditions().values()) {
				ps.setObject(cnt, value);
				cnt++;
			}
			// SQL実行
			ResultSet result = ps.executeQuery();

			// 取得結果Mapの取得
			resultList = getResultList(result);
		} catch (SQLException e) {
			// Error処理
			e.printStackTrace();
		}

		return resultList;
	}

	private Object executeInsert(Query query) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object executeUpdate(Query query) {
		try {

			String sql = createUpdateSql(query);

			PreparedStatement ps = con.prepareStatement(sql);

			int index = 1;

			// SET句
			for (Object value : query.getValues().values()) {

				ps.setObject(index++, value);
			}

			// WHERE句
			for (Object value : query.getConditions().values()) {

				ps.setObject(index++, value);
			}

			return ps.executeUpdate();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Object executeDelete(Query query) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Update SQL発行処理
	 * 
	 * @param tableName  テーブル名
	 * @param columnInfo SQLカラム名
	 * @param columnVal  SQL設定値
	 * @return 実行結果フラグ
	 */
	public boolean updateSQL(String tableName, List<String> columnInfo, List<String> columnVal) {
		return true;
	}

	private String createColumnInfo(List<String> columnInfo) {
		StringBuilder sb = new StringBuilder();
		for (String colum : columnInfo) {
			sb.append(colum);
			sb.append(",");
		}
		// 末尾のカンマを削除
		sb.setLength(sb.length() - 1);
		sb.append(DaoPart.SQL.SPACE);
		return sb.toString();
	}

	private String createFromInfo(String tableName) {
		StringBuilder sb = new StringBuilder();
		sb.append(DaoPart.SQL.FROM);
		sb.append(DaoPart.SQL.SPACE);
		if (tableName != null && !tableName.isEmpty()) {
			sb.append(tableName);
		}
		return sb.toString();
	}

	private String createWhereInfo(Map<String, Object> conditions) {
		StringBuilder sb = new StringBuilder();
		sb.append(DaoPart.SQL.SPACE);
		sb.append(DaoPart.SQL.WHEHE);
		sb.append(DaoPart.SQL.SPACE);

		for (String key : conditions.keySet()) {
			sb.append(key);
			sb.append(" = ?,");
		}
		sb.setLength(sb.length() - 1);
		sb.append(DaoPart.SQL.SPACE);

		return sb.toString();
	}

	private String createQuerySubString(Map<String, List<String>> querySubInfo) {
		StringBuilder sb = new StringBuilder();
		if (querySubInfo == null || querySubInfo.isEmpty()) {
			return "";
		}

		for (String key : querySubInfo.keySet()) {

			List<String> subQueries = querySubInfo.get(key);
			for (String subQuery : subQueries) {
				sb.append(subQuery);
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	private List<Map<String, Object>> getResultList(ResultSet rs) throws SQLException {
		List<Map<String, Object>> resultList = new ArrayList<>();
		while (rs.next()) {
			Map<String, Object> row = new HashMap<>();
			for (String column : selectInfo) {
				row.put(
						column,
						rs.getObject(column));
			}

			resultList.add(row);
		}

		return resultList;
	}

	private String createUpdateSql(Query query) {

		StringBuilder sb = new StringBuilder();

		sb.append("UPDATE ");
		sb.append(query.getTableName());

		sb.append(" SET ");

		for (String key : query.getValues().keySet()) {
			sb.append(key);
			sb.append(" = ?,");
		}

		sb.setLength(sb.length() - 1);

		sb.append(createWhereInfo(query.getConditions()));

		return sb.toString();
	}
}