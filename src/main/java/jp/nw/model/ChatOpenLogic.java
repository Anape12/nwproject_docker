package jp.nw.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import jp.nw.entity.ChatMessageEntity;
import jp.nw.entity.UserEntity;
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
        List<Map<String, Object>> result = (List<Map<String, Object>>) dbBase.execute(query);

        List<ChatMessageEntity> retList = new ArrayList<>();
        for (Map<String, Object> rowInfo : result) {
            int messageId = (int) rowInfo.get("message_id");
            String room_Id = (String) rowInfo.get("room_id");
            String postedById = (String) rowInfo.get("posted_by_id");
            String message = (String) rowInfo.get("message");
            LocalDateTime createdAt = (LocalDateTime) rowInfo.get("m.created_at");
            String postedByName = (String) rowInfo.get("posted_by_name");


            HttpSession session = request.getSession();

            retList.add(
                ChatMessageEntity.builder()
                    .messageId(messageId)
                    .roomId(room_Id)
                    .postedById(postedById)
                    .message(message)
                    .postedByName(postedByName)
                    .userEntity((UserEntity)session.getAttribute("loginUser"))
                    .createdAt(createdAt)
                    .build()
            );
        }

        return retList;
    }
}
