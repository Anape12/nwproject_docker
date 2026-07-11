package jp.nw.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jp.nw.entity.PermissionMasterEntity;
import jp.nw.parts.DBBase;
import jp.nw.parts.Query;
import jp.nw.parts.SqlType;

public class PermissionGetUtil {

    // SQL発行オブジェクト
    private static DBBase dbCon = null;

    // 権限レベル情報を全件取得
    public static List<PermissionMasterEntity> getAllPermissionLevels() {
        List<PermissionMasterEntity> permissionLevels = new ArrayList<>();

        // 取得項目
        List<String> colList = new ArrayList<String>();
        colList.add("permission_id");
        colList.add("permission_name");
        colList.add("display_order");

        // 検索条件
        LinkedHashMap<String, Object> conditions = new LinkedHashMap<>();
        conditions.put("delete_flg", "0");

        Query query = Query.builder()
                .sqlType(SqlType.SELECT)
                .selectColumns(colList)
                .tableName("permission_mst")
                .conditions(conditions)
                .build();

        dbCon = new DBBase();
        List<Map<String, Object>> result = (List<Map<String, Object>>) dbCon.execute(query);
        // ここで権限レベル情報を取得する処理を実装する

        for (Map<String, Object> rowInfo : result) {
            PermissionMasterEntity permissionEntity = PermissionMasterEntity.builder()
                    .permissionId((String) rowInfo.get("permission_id"))
                    .permissionName((String) rowInfo.get("permission_name"))
                    .displayOrder((Integer) rowInfo.get("display_order"))
                    .build();
            permissionLevels.add(permissionEntity);
        }
        return permissionLevels;
    }
}
