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
	// WHERE句情報一時退避領域
	private List<String> whereInfo = null;
	// 発行SQL
	private StringBuilder sb = null;
	// SQL結果返却用Map
	private Map<String, Object> resultMap = null;
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

	/**
	 * SELECT SQL発行処理
	 * 
	 * @param sqlWardInfo カラム名
	 * @param searchInfo  条件句
	 * @param tableName   テーブル名
	 * @return Map SQL実行結果
	 */
	public Map<String, Object> userInfoSql(Query query) {
		try {
			// Map初期化
			resultMap = new HashMap<String, Object>();
			dupliMap = new HashMap<String, Map<String, Object>>();

			// 一時退避領域へ各パラメータを格納
			this.selectInfo = query.getSelectColumns();
			this.whereInfo = new ArrayList<String>();
			for(String key : query.getConditions().keySet()) {
				this.whereInfo.add(key);
			}

			// 検索情報一括取得
			List<String> searchInfoList = new ArrayList<String>();
			for(String key : this.whereInfo) {
				searchInfoList.add((String) query.getConditions().get(key));
			}

			// SQL発行
			sb = new StringBuilder();
			sb.append(DaoPart.SQL.SELECT);
			sb.append(DaoPart.SQL.SPACE);

			// 取得情報の構築
			sb.append(createColumnInfo(this.selectInfo));
			// テーブル情報の設定
			sb.append(createFromInfo(query.getTableName()));
			// 条件句の構築
			sb.append(createWhereInfo(this.whereInfo));

			// 結果加工条件
			boolean prcInfo = query.getConditions().containsKey(DaoPart.KOMOKU_INFO.PROCESS_INFO);
			// 条件句が存在するかチェック
			if (prcInfo) {
				sb.append(createProcessInfo(query.getConditions()));
			}

			// バインド変数定義
			PreparedStatement ps = con.prepareStatement(sb.toString());

			// バインド変数設定
			int cnt = 1;
			for (int j = 0; j < searchInfoList.size(); j++) {
				ps.setString(cnt, searchInfoList.get(j));
				cnt++;
			}
			// SQL実行
			ResultSet result = ps.executeQuery();

			// 取得結果Mapの取得
			resultMap = getResultMap(result);
		} catch (SQLException e) {
			// Error処理
			e.printStackTrace();
		}

		return resultMap;
	}

	/**
	 * シンプルSelectSQL
	 */
	public List<List<String>> selectSql(String tableName, List<String> columInfo, Map<String, String> whereInfo) {

		// 検索条件取得Key
		List<String> keyList = new ArrayList<String>();

		// 検索条件返却リスト
		List<List<String>> retList = new ArrayList<List<String>>();
		try {
			// SQL発行
			sb = new StringBuilder();
			sb.append(DaoPart.SQL.SELECT);
			sb.append(DaoPart.SQL.SPACE);

			for (String columVal : columInfo) {
				sb.append(columVal);
				sb.append(",");
			}
			// 末尾のカンマを削除
			sb.setLength(sb.length() - 1);

			sb.append(DaoPart.SQL.SPACE);
			sb.append(DaoPart.SQL.FROM);
			sb.append(DaoPart.SQL.SPACE);
			if (tableName != null || tableName != "") {
				sb.append(tableName);
			}
			sb.append(DaoPart.SQL.SPACE);
			sb.append(DaoPart.SQL.WHEHE);
			sb.append(DaoPart.SQL.SPACE);

			// 条件句存在チェック
			if (whereInfo.size() != 0) {
				for (String key : whereInfo.keySet()) {
					// 条件句の場合処理をスルー
					if (key.equals(DaoPart.KOMOKU_INFO.PROCESS_INFO)) {
						continue;
					}
					// バインド化用Key
					keyList.add(key);
					sb.append(key);
					sb.append(DaoPart.SQL.SPACE);
					sb.append(DaoPart.SQL.EQUARL);
					sb.append(DaoPart.SQL.SPACE);
					sb.append("?");
					sb.append(",");
				}
				// 末尾のカンマを削除
				sb.setLength(sb.length() - 1);
			}

			// データ操作句が存在する場合
			if (whereInfo.containsKey(DaoPart.KOMOKU_INFO.PROCESS_INFO)) {
				sb.append(DaoPart.SQL.SPACE);
				sb.append(whereInfo.get(DaoPart.KOMOKU_INFO.PROCESS_INFO));
				sb.append(";");
			}

			// バインド変数定義
			PreparedStatement ps = con.prepareStatement(sb.toString());

			// バインド変数設定
			int bindCnt = 1;
			if (keyList.size() != 0) {
				for (int i = 0; i < keyList.size(); i++) {
					ps.setString(bindCnt, whereInfo.get(keyList.get(i)));
					bindCnt++;
				}
			}
			// SQL実行
			ResultSet result = ps.executeQuery();

			while (result.next()) {
				// １カラム返却情報
				List<String> colList = new ArrayList<String>();
				for (String key : columInfo) {
					colList.add(result.getString(key));
				}
				retList.add(colList);
			}
			// SQL結果返却
			return retList;
		} catch (SQLException e) {
			// Error処理
			e.printStackTrace();
		}
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

	private String createWhereInfo(List<String> whereInfo) {
		StringBuilder sb = new StringBuilder();

		sb.append(DaoPart.SQL.SPACE);
		sb.append(DaoPart.SQL.WHEHE);
		sb.append(DaoPart.SQL.SPACE);

		for (int i = 0; i < whereInfo.size(); i++) {
			sb.append(whereInfo.get(i));
			sb.append(DaoPart.SQL.SPACE);
			sb.append(DaoPart.SQL.EQUARL);
			sb.append(DaoPart.SQL.SPACE);
			sb.append("?");
			sb.append(",");
		}
		// 末尾のカンマを削除
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	private String createProcessInfo(Map<String, Object> sqlWardInfo) {
		StringBuilder sb = new StringBuilder();
		String prcRs = (String) sqlWardInfo.get(DaoPart.KOMOKU_INFO.PROCESS_INFO);
		String[] info = prcRs.split(";");
		for (int j = 0; j < info.length; j++) {
			sb.append(DaoPart.SQL.SPACE);
			sb.append(info[j]);
		}
		return sb.toString();
	}

	private Map<String, Object> getResultMap(ResultSet result) throws SQLException {
		Map<String, Object> resultMap = new HashMap<>();
		while (result.next()) {
			for (int k = 0; k < this.selectInfo.size(); k++) {
				// Key重複チェック
				if (resultMap.containsKey(this.selectInfo.get(k))) {
					Integer i = Integer.valueOf(k);
					String str = i.toString();
					deuliKey = "DKEY".concat(str);
					dupliMap.put(deuliKey, resultMap);
				} else {
					resultMap.put(this.selectInfo.get(k), result.getString(this.selectInfo.get(k)));
				}
			}
		}
		return resultMap;
	}

}