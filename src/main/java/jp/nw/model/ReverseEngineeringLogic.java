package jp.nw.model;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;

import jp.nw.parts.DBBase;

/** Read-only reverse engineering of the live schema and deployed application classes. */
public class ReverseEngineeringLogic {
    private static final String APPLICATION_PACKAGE = "jp.nw.";

    public Map<String, Object> database() throws SQLException {
        DBBase db = new DBBase();
        try (Connection connection = db.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            String catalog = connection.getCatalog();
            List<Map<String, Object>> tables = new ArrayList<>();
            List<Map<String, Object>> relations = new ArrayList<>();
            List<String> tableNames = new ArrayList<>();
            try (ResultSet result = metadata.getTables(catalog, null, "%", new String[] { "TABLE" })) {
                while (result.next()) {
                    String tableName = result.getString("TABLE_NAME");
                    if (tableName != null) {
                        tableNames.add(tableName);
                    }
                }
            }
            for (String tableName : tableNames) {
                tables.add(table(metadata, catalog, tableName, relations));
            }
            tables.sort(Comparator.comparing(table -> (String) table.get("name")));
            Map<String, Object> output = new LinkedHashMap<>();
            output.put("database", catalog);
            output.put("tables", tables);
            output.put("relations", relations);
            return output;
        }
    }

    private Map<String, Object> table(DatabaseMetaData metadata, String catalog, String name,
            List<Map<String, Object>> relations) throws SQLException {
        Set<String> primaryKeys = new TreeSet<>();
        try (ResultSet result = metadata.getPrimaryKeys(catalog, null, name)) {
            while (result.next()) {
                primaryKeys.add(result.getString("COLUMN_NAME"));
            }
        }

        List<Map<String, Object>> columns = new ArrayList<>();
        try (ResultSet result = metadata.getColumns(catalog, null, name, "%")) {
            while (result.next()) {
                Map<String, Object> column = new LinkedHashMap<>();
                String columnName = result.getString("COLUMN_NAME");
                column.put("name", columnName);
                column.put("type", result.getString("TYPE_NAME"));
                column.put("size", result.getInt("COLUMN_SIZE"));
                column.put("nullable", result.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                column.put("primaryKey", primaryKeys.contains(columnName));
                column.put("defaultValue", result.getString("COLUMN_DEF"));
                column.put("remarks", result.getString("REMARKS"));
                column.put("ordinal", result.getInt("ORDINAL_POSITION"));
                columns.add(column);
            }
        }
        columns.sort(Comparator.comparingInt(column -> (Integer) column.get("ordinal")));

        try (ResultSet result = metadata.getImportedKeys(catalog, null, name)) {
            while (result.next()) {
                Map<String, Object> relation = new LinkedHashMap<>();
                relation.put("fromTable", name);
                relation.put("fromColumn", result.getString("FKCOLUMN_NAME"));
                relation.put("toTable", result.getString("PKTABLE_NAME"));
                relation.put("toColumn", result.getString("PKCOLUMN_NAME"));
                relation.put("constraint", result.getString("FK_NAME"));
                relations.add(relation);
            }
        }

        Map<String, Object> table = new LinkedHashMap<>();
        table.put("name", name);
        table.put("columns", columns);
        return table;
    }

    public Map<String, Object> javaClasses(ServletContext context) {
        Set<String> names = new TreeSet<>();
        collectClasses(context, "/WEB-INF/classes/jp/nw/", names);
        List<Map<String, Object>> classes = new ArrayList<>();
        List<Map<String, Object>> relations = new ArrayList<>();
        ClassLoader loader = context.getClassLoader();
        for (String name : names) {
            try {
                Class<?> type = Class.forName(name, false, loader);
                if (type.isSynthetic() || type.isAnonymousClass() || type.isLocalClass()) {
                    continue;
                }
                classes.add(javaClass(type, relations));
            } catch (ClassNotFoundException | LinkageError | SecurityException | TypeNotPresentException ignored) {
                // An optional or unloadable class must not hide the rest of the diagram.
            }
        }
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("classes", classes);
        output.put("relations", relations);
        return output;
    }

    private void collectClasses(ServletContext context, String path, Set<String> names) {
        Set<String> resources = context.getResourcePaths(path);
        if (resources == null) {
            return;
        }
        for (String resource : resources) {
            if (resource.endsWith("/")) {
                collectClasses(context, resource, names);
            } else if (resource.endsWith(".class") && !resource.contains("$")) {
                names.add(resource.substring("/WEB-INF/classes/".length(), resource.length() - 6)
                        .replace('/', '.'));
            }
        }
    }

    private Map<String, Object> javaClass(Class<?> type, List<Map<String, Object>> relations) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", type.getName());
        item.put("simpleName", type.getSimpleName());
        item.put("packageName", type.getPackageName());
        item.put("kind", type.isInterface() ? "interface" : type.isEnum() ? "enum" : "class");

        List<Map<String, String>> fields = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            fields.add(Map.of("name", field.getName(), "type", displayType(field.getGenericType())));
            addTypeRelations(relations, type, field.getGenericType(), "field");
        }
        fields.sort(Comparator.comparing(field -> field.get("name")));
        item.put("fields", fields);

        List<String> methods = new ArrayList<>();
        try {
            for (Method method : type.getDeclaredMethods()) {
                if (!method.isSynthetic() && !method.isBridge() && Modifier.isPublic(method.getModifiers())) {
                    StringBuilder signature = new StringBuilder(method.getName()).append('(');
                    Type[] parameters = method.getGenericParameterTypes();
                    for (int index = 0; index < parameters.length; index++) {
                        if (index > 0) signature.append(", ");
                        signature.append(displayType(parameters[index]));
                        addTypeRelations(relations, type, parameters[index], "parameter");
                    }
                    methods.add(signature.append(") : ").append(displayType(method.getGenericReturnType())).toString());
                    addTypeRelations(relations, type, method.getGenericReturnType(), "return");
                }
            }
        } catch (LinkageError ignored) {
            // Method signatures may reference unavailable optional classes.
        }
        methods.sort(String::compareTo);
        item.put("methods", methods);

        addRelation(relations, type, type.getSuperclass(), "extends");
        for (Class<?> interfaceType : type.getInterfaces()) {
            addRelation(relations, type, interfaceType, "implements");
        }
        return item;
    }

    private void addRelation(List<Map<String, Object>> relations, Class<?> from, Class<?> to, String kind) {
        if (to == null || !to.getName().startsWith(APPLICATION_PACKAGE) || from.equals(to)) {
            return;
        }
        Map<String, Object> relation = new LinkedHashMap<>();
        relation.put("from", from.getName());
        relation.put("to", to.getName());
        relation.put("kind", kind);
        if (!relations.contains(relation)) {
            relations.add(relation);
        }
    }

    private void addTypeRelations(List<Map<String, Object>> relations, Class<?> from, Type type, String kind) {
        if (type instanceof Class<?> classType) {
            addRelation(relations, from, classType.isArray() ? classType.getComponentType() : classType, kind);
        } else if (type instanceof ParameterizedType parameterized) {
            addTypeRelations(relations, from, parameterized.getRawType(), kind);
            for (Type argument : parameterized.getActualTypeArguments()) {
                addTypeRelations(relations, from, argument, kind);
            }
        } else if (type instanceof GenericArrayType arrayType) {
            addTypeRelations(relations, from, arrayType.getGenericComponentType(), kind);
        } else if (type instanceof WildcardType wildcard) {
            for (Type bound : wildcard.getUpperBounds()) addTypeRelations(relations, from, bound, kind);
            for (Type bound : wildcard.getLowerBounds()) addTypeRelations(relations, from, bound, kind);
        }
    }

    private String displayType(Type type) {
        return type.getTypeName().replace("java.lang.", "").replace("java.util.", "");
    }
}
