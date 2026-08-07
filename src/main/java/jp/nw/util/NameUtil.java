package jp.nw.util;

public class NameUtil {
    
    public static String toSnakeCase(String camel) {
        return camel.replaceAll(
                "([a-z])([A-Z])",
                "$1_$2")
                .toLowerCase();
    }
}
