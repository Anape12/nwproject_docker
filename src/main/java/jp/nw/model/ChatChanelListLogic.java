package jp.nw.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jp.nw.entity.ChatRoomEntity;
import jp.nw.parts.DBBase;
import jp.nw.parts.JoinInfo;
import jp.nw.parts.JoinType;
import jp.nw.parts.Query;
import jp.nw.parts.SqlType;

public class ChatChanelListLogic {
 
    DBBase dbBase = null;

    public List<ChatRoomEntity> getTargetChanel(String targetId){
        

        // カラム情報
        List<String> colList = new ArrayList<>();
        colList.add("r.room_id");
        colList.add("r.room_type");
        colList.add(
            "CASE " +
            "WHEN r.room_type = '1' THEN (" +
            "   SELECT CONCAT(u.last_name, ' ', u.first_name) " +
            "   FROM chat_room_member m2 " +
            "   INNER JOIN users_info u ON u.user_id = m2.user_id " +
            "   WHERE m2.room_id = r.room_id " +
            "     AND m2.user_id <> m.user_id " +
            "   LIMIT 1" +
            ") " +
            "ELSE r.room_name " +
            "END AS display_name"
        );

        // 検索条件
        LinkedHashMap<String, Object> conditions = new LinkedHashMap<>();
        conditions.put("m.user_id", targetId);
        conditions.put("r.delete_flg", "0");

        Map<String, List<String>> querySub = new HashMap<>();
        querySub.put("PROCESS_INFO", Arrays.asList("ORDER BY r.updated_at DESC"));

        Query query = Query.builder()
        .sqlType(SqlType.SELECT)
        .tableName("chat_room r")
        .joins(List.of(
            JoinInfo.builder()
                .joinType(JoinType.INNER)
                .tableName("chat_room_member m")
                .condition("r.room_id = m.room_id")
                .build()
        ))
        .selectColumns(colList)
        .conditions(conditions)
        .querySub(querySub)
        .build();

        dbBase = new DBBase();

        return dbBase.execute(query, ChatRoomEntity.class);
    }
}
