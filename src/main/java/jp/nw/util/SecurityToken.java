package jp.nw.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jp.nw.parts.DBBase;
import jp.nw.parts.Query;
import jp.nw.parts.SqlType;

public class SecurityToken {

    public static boolean updateToken(String userId, String token) {

        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        values.put("current_login_token", token);
        LinkedHashMap<String, Object> conditions = new LinkedHashMap<>();
        conditions.put("user_id", userId);

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
            return false;
        }

        return true;
    }

    public static String getToken(String userId) {
        // 取得項目
        List<String> colList = new ArrayList<String>();
        colList.add("current_login_token");

        // 検索条件
        LinkedHashMap<String, Object> conditions = new LinkedHashMap<>();
        conditions.put("user_id", userId);

        Query query = Query.builder()
                .sqlType(SqlType.SELECT)
                .selectColumns(colList)
                .tableName("users_info")
                .conditions(conditions)
                .build();

        DBBase db = new DBBase();
        List<Map<String, Object>> result = (List<Map<String, Object>>) db.execute(query);

        for (Map<String, Object> rowInfo : result) {
            Object tokenValue = rowInfo.get("current_login_token");
            if (tokenValue != null) {
                return tokenValue.toString();
            }
        }

        return null; // トークンが見つからなかった場合
    }
}
