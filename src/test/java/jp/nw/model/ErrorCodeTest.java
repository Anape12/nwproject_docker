package jp.nw.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class ErrorCodeTest {
    @Test
    void catalogCodesAreUniqueAndWellFormed() {
        Set<String> codes = Arrays.stream(ErrorCode.values()).map(ErrorCode::code).collect(Collectors.toSet());
        assertEquals(ErrorCode.values().length, codes.size());
        assertTrue(codes.stream().allMatch(ErrorMessageLogic::isValidCode));
        assertEquals("ERR00000001", ErrorCode.AUTH_001.code());
        assertEquals("ERR00010000", ErrorCode.APP_000.code());
        assertEquals("ERR00010139", ErrorCode.APP_139.code());
    }
}
