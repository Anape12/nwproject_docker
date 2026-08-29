package jp.nw.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jp.nw.entity.ChatMessageEntity;
import jp.nw.parts.DBBase;
import jp.nw.parts.JoinInfo;
import jp.nw.parts.JoinType;
import jp.nw.parts.Query;
import jp.nw.parts.SqlType;

public class ChatOpenLogic {
    
    DBBase dbBase = null;

    public List<ChatMessageEntity> getChanelOpen(String roomId, HttpServletRequest request){

        Query query = Query.builder()
            .sqlType(SqlType.SELECT)
            .tableName("chat_message m")
            .joins(List.of(
                JoinInfo.builder()
                    .joinType(JoinType.INNER)
                    .tableName("users_info u")
                    .condition("u.user_id = m.posted_by_id")
                    .build()
            ))
            .selectColumns(List.of(
                "m.message_id",
                "m.room_id",
                "m.posted_by_id",
                "CONCAT(u.last_name, ' ', u.first_name) AS posted_by_name",
                "u.account_type AS posted_by_account_type",
                "m.message",
                "m.created_at"
            ))
            .conditions(new LinkedHashMap<>() {{
                put("m.room_id", roomId);
                put("m.delete_flg", "0");
            }})
            .querySub(Map.of(
                "PROCESS_INFO", List.of("ORDER BY m.created_at ASC")
            ))
            .build();

        dbBase = new DBBase();

        return dbBase.execute(query, ChatMessageEntity.class);
    }
}
