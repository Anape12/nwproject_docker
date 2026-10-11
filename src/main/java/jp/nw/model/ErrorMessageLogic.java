package jp.nw.model;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import jp.nw.parts.DBBase;

/** Resolves user-facing messages from the database catalog. */
public final class ErrorMessageLogic {
    private static final Pattern CODE_FORMAT = Pattern.compile("(?:ERR|WAR)[0-9]{8}");
    private static final Logger LOGGER = Logger.getLogger(ErrorMessageLogic.class.getName());
    private static final String FALLBACK_MESSAGE = loadFallbackMessage();
    private static final MessageCache CACHE = new MessageCache(
            Duration.ofMinutes(5), Duration.ofSeconds(30), System::nanoTime);

    private ErrorMessageLogic() {
    }

    public static String find(Connection connection, String errorCode) throws SQLException {
        if (!isValidCode(errorCode)) {
            throw new IllegalArgumentException("エラーコードはERRまたはWARと8桁の数字で指定してください。");
        }
        String sql = "SELECT message_text FROM error_message_mst WHERE error_code=? AND enabled=TRUE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, errorCode);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    String message = result.getString(1);
                    if (message != null && !message.isBlank())
                        return message;
                }
            }
        }
        throw new SQLException("有効なエラーメッセージが登録されていません: " + errorCode);
    }

    static boolean isValidCode(String errorCode) {
        return errorCode != null && CODE_FORMAT.matcher(errorCode).matches();
    }

    private static String get(String errorCode) {
        return CACHE.get(errorCode, () -> {
            try (Connection connection = new DBBase().getConnection()) {
                return find(connection, errorCode);
            }
        });
    }

    public static String get(ErrorCode errorCode) {
        return get(errorCode.code());
    }

    public static String find(Connection connection, ErrorCode errorCode) throws SQLException {
        return find(connection, errorCode.code());
    }

    /** Uses the current transaction's connection without opening a second one. */
    public static String get(Connection connection, ErrorCode errorCode) {
        return CACHE.get(errorCode.code(), () -> find(connection, errorCode));
    }

    /**
     * Never show an arbitrary exception message (including SQL details) to a user.
     */
    public static String forDisplay(Throwable error) {
        return get(errorCode(error));
    }

    static ErrorCode errorCode(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof CodedException.Coded coded)
                return coded.errorCode();
            current = current.getCause();
        }
        return ErrorCode.APP_000;
    }

    private static String loadFallbackMessage() {
        try (InputStream stream = ErrorMessageLogic.class
                .getResourceAsStream("/jp/nw/model/error-message-fallback.properties")) {
            if (stream != null) {
                Properties properties = new Properties();
                properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
                String message = properties.getProperty("system_error");
                if (message != null && !message.isBlank())
                    return message.trim();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to load the error-message fallback resource", e);
        }
        return "処理を完了できませんでした。時間をおいて再度お試しください。";
    }

    @FunctionalInterface
    interface MessageLoader {
        String load() throws Exception;
    }

    static final class MessageCache {
        private final ConcurrentHashMap<String, Entry> entries = new ConcurrentHashMap<>();
        private final long successNanos;
        private final long retryNanos;
        private final LongSupplier ticker;

        MessageCache(Duration successTtl, Duration failureRetry, LongSupplier ticker) {
            this.successNanos = successTtl.toNanos();
            this.retryNanos = failureRetry.toNanos();
            this.ticker = ticker;
        }

        String get(String code, MessageLoader loader) {
            if (!isValidCode(code)) {
                throw new IllegalArgumentException("エラーコードはERRまたはWARと8桁の数字で指定してください。");
            }
            return entries.compute(code, (key, previous) -> {
                long now = ticker.getAsLong();
                if (previous != null && now - previous.refreshAtNanos < 0)
                    return previous;
                try {
                    String message = loader.load();
                    if (message == null || message.isBlank())
                        throw new SQLException("Empty message for " + key);
                    return new Entry(message, now + successNanos);
                } catch (Exception | LinkageError e) {
                    LOGGER.log(Level.WARNING, "Unable to load message for " + key + "; using cached or fallback text",
                            e);
                    String message = previous == null ? FALLBACK_MESSAGE + " (" + key + ")" : previous.message;
                    return new Entry(message, now + retryNanos);
                }
            }).message;
        }

        private record Entry(String message, long refreshAtNanos) {
        }
    }
}
