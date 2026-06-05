package jp.nw.parts;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Query {
    private SqlType sqlType;

    private String tableName;

    private List<String> selectColumns;

    private Map<String, Object> values;

    private Map<String, Object> conditions;
}
