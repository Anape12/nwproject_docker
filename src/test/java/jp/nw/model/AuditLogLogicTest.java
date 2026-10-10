package jp.nw.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class AuditLogLogicTest {
    @Test
    void rendersMasterTemplateWithStructuredValues() {
        String detail = AuditLogLogic.renderDetail(null, "NOW_LOCKED",
                "失敗回数: {failedCount}、解除予定: {lockUntil}",
                "{\"failedCount\":5,\"lockUntil\":\"2026-10-10T09:15\"}");
        assertEquals("失敗回数: 5、解除予定: 2026-10-10T09:15", detail);
        assertEquals("以前の詳細", AuditLogLogic.renderDetail("以前の詳細", null, null, null));
        assertEquals("NOW_LOCKED", AuditLogLogic.renderDetail(null, "NOW_LOCKED", null, null));
        assertEquals("NOW_LOCKED", AuditLogLogic.renderDetail(null, "NOW_LOCKED",
                "解除予定: {lockUntil}", null));
        assertEquals("NOW_LOCKED", AuditLogLogic.renderDetail(null, "NOW_LOCKED",
                "解除予定: {lockUntil}", "invalid json"));
    }

    @Test
    void recordsReasonAndJsonWithoutHardCodedDetail() throws Exception {
        Map<Integer, String> values = new HashMap<>();
        PreparedStatement statement = (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] { PreparedStatement.class }, (proxy, method, arguments) -> {
                    if ("setString".equals(method.getName())) values.put((Integer) arguments[0], (String) arguments[1]);
                    if ("executeUpdate".equals(method.getName())) return 1;
                    return null;
                });
        Connection connection = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] { Connection.class }, (proxy, method, arguments) -> {
                    if ("prepareStatement".equals(method.getName())) return statement;
                    return null;
                });

        AuditLogLogic.recordStructured(connection, "user1", "AUTH", "ACCOUNT_LOCKED", "USER", "user1",
                false, "127.0.0.1", "test", "NOW_LOCKED",
                Map.of("failedCount", 5, "lockUntil", "2026-10-10T09:15"));

        assertNull(values.get(9));
        assertEquals("NOW_LOCKED", values.get(10));
        JsonNode json = new ObjectMapper().readTree(values.get(11));
        assertEquals(5, json.get("failedCount").asInt());
        assertEquals("2026-10-10T09:15", json.get("lockUntil").asText());
    }
}
