package jp.nw.parts;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultMapper {

    public List<Map<String, Object>> toList(
            ResultSet rs,
            List<String> columns) throws SQLException {

        List<Map<String, Object>> resultList = new ArrayList<>();

        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (String column : columns) {
                row.put(column, rs.getObject(column));
            }
            resultList.add(row);
        }

        return resultList;
    }
}