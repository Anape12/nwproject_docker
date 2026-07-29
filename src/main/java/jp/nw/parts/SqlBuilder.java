package jp.nw.parts;

import java.util.List;
import java.util.Map;

public class SqlBuilder {

    public String build(Query query) {

        switch (query.getSqlType()) {

            case SELECT:
                return buildSelect(query);

            case INSERT:
                return buildInsert(query);

            case UPDATE:
                return buildUpdate(query);

            case DELETE:
                return buildDelete(query);

            default:
                throw new IllegalArgumentException(
                        "Unsupported SQL Type : " + query.getSqlType());
        }
    }

    /**
     * SELECT文生成
     */
    private String buildSelect(Query query) {

        StringBuilder sb = new StringBuilder();

        sb.append("SELECT ");
        if(query.getSelectSub() != null && !query.getSelectSub().isBlank()){
            sb.append(query.getSelectSub()).append(" ");
        }
        sb.append(createColumnInfo(query.getSelectColumns()));

        sb.append("FROM ");
        sb.append(query.getTableName());

        sb.append(createJoinInfo(query.getJoins()));

        if (!query.getConditions().isEmpty()) {
            sb.append(createWhereInfo(query.getConditions()));
        }

        sb.append(createQuerySubString(query.getQuerySub()));

        return sb.toString();
    }

    /**
     * INSERT文生成
     */
    private String buildInsert(Query query) {

        StringBuilder sb = new StringBuilder();

        sb.append("INSERT INTO ");
        sb.append(query.getTableName());

        sb.append(" (");

        for (String key : query.getValues().keySet()) {
            sb.append(key);
            sb.append(",");
        }

        sb.setLength(sb.length() - 1);

        sb.append(") VALUES (");

        for (int i = 0; i < query.getValues().size(); i++) {
            sb.append("?,");
        }

        sb.setLength(sb.length() - 1);

        sb.append(")");

        return sb.toString();
    }

    /**
     * UPDATE文生成
     */
    private String buildUpdate(Query query) {

        StringBuilder sb = new StringBuilder();

        sb.append("UPDATE ");
        sb.append(query.getTableName());

        sb.append(" SET ");

        for (String key : query.getValues().keySet()) {
            sb.append(key);
            sb.append(" = ?,");
        }

        sb.setLength(sb.length() - 1);

        if (!query.getConditions().isEmpty()) {
            sb.append(createWhereInfo(query.getConditions()));
        }

        return sb.toString();
    }

    /**
     * DELETE文生成
     */
    private String buildDelete(Query query) {

        StringBuilder sb = new StringBuilder();

        sb.append("DELETE FROM ");
        sb.append(query.getTableName());

        if (!query.getConditions().isEmpty()) {
            sb.append(createWhereInfo(query.getConditions()));
        }

        return sb.toString();
    }

    /**
     * SELECT列生成
     */
    private String createColumnInfo(List<String> columns) {

        StringBuilder sb = new StringBuilder();

        if (columns == null || columns.isEmpty()) {
            sb.append("* ");
            return sb.toString();
        }

        for (String column : columns) {
            sb.append(column);
            sb.append(",");
        }

        sb.setLength(sb.length() - 1);

        sb.append(" ");

        return sb.toString();
    }

    /**
     * WHERE句生成
     */
    private String createWhereInfo(Map<String, Object> conditions) {

        StringBuilder sb = new StringBuilder();

        sb.append(" WHERE ");

        for (String key : conditions.keySet()) {

            sb.append(key);
            sb.append(" = ? AND ");
        }

        sb.setLength(sb.length() - 5);

        sb.append(" ");

        return sb.toString();
    }

    /**
     * ORDER BY等生成
     */
    private String createQuerySubString(
            Map<String, List<String>> querySubInfo) {

        if (querySubInfo == null || querySubInfo.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (List<String> list : querySubInfo.values()) {

            for (String sql : list) {

                sb.append(sql);
                sb.append(" ");
            }
        }

        return sb.toString();
    }

    private String createJoinInfo(List<JoinInfo> joins) {
        if (joins == null || joins.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (JoinInfo join : joins) {

            switch (join.getJoinType()) {

            case INNER:
                sb.append(" INNER JOIN ");
                break;

            case LEFT:
                sb.append(" LEFT JOIN ");
                break;

            case RIGHT:
                sb.append(" RIGHT JOIN ");
                break;
            }

            sb.append(join.getTableName());
            sb.append(" ON ");
            sb.append(join.getCondition());
        }

        sb.append(" ");

        return sb.toString();
    }

}