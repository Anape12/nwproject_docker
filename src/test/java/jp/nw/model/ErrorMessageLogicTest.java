package jp.nw.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

class ErrorMessageLogicTest {
    @Test
    void resolvesEnabledMessageByCode() throws Exception {
        AtomicReference<String> boundCode = new AtomicReference<>();
        Connection connection = connection("DBで設定した文言", boundCode);
        assertEquals("DBで設定した文言", ErrorMessageLogic.find(connection, ErrorCode.AUTH_001));
        assertEquals("ERR00000001", boundCode.get());
    }

    @Test
    void acceptsEightDigitWarningCodesAndRejectsHyphens() throws Exception {
        assertTrue(ErrorMessageLogic.isValidCode("ERR00000001"));
        assertTrue(ErrorMessageLogic.isValidCode("WAR00000001"));
        assertThrows(IllegalArgumentException.class,
                () -> ErrorMessageLogic.find(connection("old", new AtomicReference<>()), "AUTH-001"));
        assertThrows(IllegalArgumentException.class,
                () -> ErrorMessageLogic.find(connection("short", new AtomicReference<>()), "ERR001"));
    }

    @Test
    void missingMessageIsAConfigurationError() {
        SQLException error = assertThrows(SQLException.class,
                () -> ErrorMessageLogic.find(connection(null, new AtomicReference<>()), "ERR00000999"));
        assertTrue(error.getMessage().contains("ERR00000999"));
    }

    @Test
    void resolvesCodeFromWrappedFailureWithoutShowingExceptionDetails() {
        RuntimeException wrapped = new RuntimeException("database detail",
                new CodedException.Failure(ErrorCode.APP_115, new SQLException("secret SQL")));
        assertEquals(ErrorCode.APP_115, ErrorMessageLogic.errorCode(wrapped));
        assertEquals(ErrorCode.APP_000, ErrorMessageLogic.errorCode(new SQLException("secret SQL")));
    }

    private Connection connection(String message, AtomicReference<String> boundCode) {
        AtomicInteger rows = new AtomicInteger();
        ResultSet result = (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] { ResultSet.class }, (proxy, method, arguments) -> switch (method.getName()) {
                    case "next" -> message != null && rows.getAndIncrement() == 0;
                    case "getString" -> message;
                    default -> null;
                });
        PreparedStatement statement = (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] { PreparedStatement.class }, (proxy, method, arguments) -> {
                    if ("setString".equals(method.getName())) boundCode.set((String) arguments[1]);
                    if ("executeQuery".equals(method.getName())) return result;
                    return null;
                });
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] { Connection.class }, (proxy, method, arguments) -> {
                    if ("prepareStatement".equals(method.getName())) {
                        assertTrue(((String) arguments[0]).contains("enabled=TRUE"));
                        return statement;
                    }
                    return null;
                });
    }
}
