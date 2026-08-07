package jp.nw.model;

import java.util.HashMap;
import java.util.Map;

import jp.nw.entity.UserEntity;
import jp.nw.parts.DBBase;
import jp.nw.parts.Query;
import jp.nw.parts.SqlType;

public class RegistrationChatLogic {
     DBBase dbBase = null;

    public long register(UserEntity loginUser, String roomId, String comment){

        Map<String, Object> threadInfos = new HashMap<String, Object>();
        threadInfos.put("room_id" , roomId);
        threadInfos.put("message", comment);
        threadInfos.put("posted_by_id", loginUser.getUserId());

        Query query = Query.builder()
                    .sqlType(SqlType.INSERT)
                    .tableName("chat_message")
                    .values(threadInfos)
                    .build();

        dbBase = new DBBase();
        return dbBase.executeInsert(query);
    }
}
