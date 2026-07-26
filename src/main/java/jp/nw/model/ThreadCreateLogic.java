package jp.nw.model;

import java.util.Map;

import jp.nw.parts.DBBase;
import jp.nw.parts.Query;
import jp.nw.parts.SqlType;

public class ThreadCreateLogic {

    DBBase dbBase = null;

    public long insertThread(Map<String, Object> threadInfos) {

        Query query = Query.builder()
                .sqlType(SqlType.INSERT)
                .tableName("thread_info")
                .values(threadInfos)
                .build();

        dbBase = new DBBase();
        return dbBase.executeInsert(query);
    }
}
