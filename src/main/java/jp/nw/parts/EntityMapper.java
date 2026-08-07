package jp.nw.parts;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import jp.nw.util.NameUtil;

public class EntityMapper {

    /**
     * List<Map> → List<Entity>
     */
    public <T> List<T> toEntityList(List<Map<String, Object>> rows, Class<T> clazz) {
        return rows.stream()
                .map(row -> {
                    try {
                        return toEntity(row, clazz);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to map row to entity: " + e.getMessage(), e);
                    }
                }).toList();
    }

    /**
     * Map → Entity
     */
    private <T> T toEntity(
        Map<String, Object> row,
        Class<T> clazz)
        throws Exception {

        T entity = clazz.getDeclaredConstructor().newInstance();

        for (Field field : clazz.getDeclaredFields()) {

            field.setAccessible(true);

            String columnName =
                    NameUtil.toSnakeCase(field.getName());

            if(!row.containsKey(columnName)) {
                continue;
            }

            Object value = row.get(columnName);

            value = convertValue(value, field.getType());

            field.set(entity, value);
        }

        return entity;
    }

    private Object convertValue(Object value, Class<?> targetType) {

        if (value == null) {
            return null;
        }

        if(targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        if (value instanceof Timestamp &&
                targetType == LocalDateTime.class) {

            return ((Timestamp) value)
                    .toLocalDateTime();
        }

        return value;
    }
}