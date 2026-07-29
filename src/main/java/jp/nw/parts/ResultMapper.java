package jp.nw.parts;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultMapper {

    public List<Map<String, Object>> toList(ResultSet rs) throws SQLException {

        List<Map<String, Object>> resultList = new ArrayList<>();

        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        while (rs.next()) {

            Map<String, Object> row = new HashMap<>();

            for (int i = 1; i <= columnCount; i++) {
                String columnLabel = meta.getColumnLabel(i);
                row.put(columnLabel, rs.getObject(i));
            }

            resultList.add(row);
        }

        return resultList;
    }
}