package jp.nw.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateFormatUtil {
    
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateFormatUtil() {
    }

    public static String format(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME);
    }

    public static LocalDateTime parse(String value) {
        return value == null || value.isBlank()
                ? null
                : LocalDateTime.parse(value, DATE_TIME);
    }
}
