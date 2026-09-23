package jp.nw.batch;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jp.nw.parts.DBBase;

public class JobStart {

    public static final String SQL = "PLtest.sql";
	
    /**
     * Job実行関数
     * @param jobName 実行ジョブ名
     * @return
     */
	public boolean execute(String jobName) {
		DBBase db = new DBBase();
		try (var connection = db.getConnection();
				CallableStatement callableStatement = connection.prepareCall(jobName)) {
                // INパラメータの設定
                callableStatement.setString(1, "a0001");

                // ストアドプロシージャの実行
                boolean hasResultSet = callableStatement.execute();
                
                // 結果セットの処理
                if (hasResultSet) {
                    try (ResultSet resultSet = callableStatement.getResultSet()) {
                        while (resultSet.next()) {
                            // 結果セットを最後まで読み、呼び出しを正常終了させる。
                        }
                    }
				}
			return true;
		} catch (SQLException e) {
			throw new RuntimeException("ジョブの実行に失敗しました。", e);
		}
	}
}
