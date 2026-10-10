package jp.nw.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.junit.jupiter.api.Test;

import jp.nw.model.AuthenticationLogic.Decision;
import jp.nw.model.AuthenticationLogic.Outcome;
import jp.nw.model.AuthenticationLogic.UserRecord;
import jp.nw.parts.PasswordUtil;

class AuthenticationLogicTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 10, 9, 0);

    @Test
    void unknownAndBlockedAccountsKeepOriginalPriority() {
        assertEquals(Outcome.NO_USER, decide(null, "secret1").outcome());
        UserRecord record = activeUser();
        record.accountDisabled = true;
        record.accountType = "AI";
        record.lockedUntil = NOW.plusMinutes(10);
        assertEquals(Outcome.DISABLED, decide(record, "wrong").outcome());
        record.accountDisabled = false;
        record.deleteFlag = "1";
        assertEquals(Outcome.DISABLED, decide(record, "wrong").outcome());
        record.deleteFlag = "0";
        assertEquals(Outcome.AI_ACCOUNT, decide(record, "wrong").outcome());
        record.accountType = "HUMAN";
        assertEquals(Outcome.ALREADY_LOCKED, decide(record, "wrong").outcome());
    }

    @Test
    void passwordFailuresIncrementAndLockAtThreshold() {
        UserRecord record = activeUser();
        record.failedCount = 3;
        Decision failure = decide(record, "wrong");
        assertEquals(Outcome.BAD_PASSWORD, failure.outcome());
        assertEquals(4, failure.failures());
        assertNull(failure.lockUntil());
        assertEquals(Map.of("failedCount", 4), failure.auditData());
        record.failedCount = 4;
        Decision locked = decide(record, "wrong");
        assertEquals(Outcome.NOW_LOCKED, locked.outcome());
        assertEquals(5, locked.failures());
        assertEquals(NOW.plusMinutes(15), locked.lockUntil());
        assertEquals(Map.of("failedCount", 5, "lockUntil", NOW.plusMinutes(15).toString()),
                locked.auditData());
        AuthenticationLogic.Result result = locked.toResult("user1", record, "DBから取得したメッセージ");
        assertFalse(result.authenticated());
        assertEquals("ERR00000005", result.errorCode());
        assertEquals("DBから取得したメッセージ", result.message());
    }

    @Test
    void expirationIsCheckedOnlyAfterPasswordMatches() {
        UserRecord record = activeUser();
        record.passwordExpiration = NOW.minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE);
        assertEquals(Outcome.BAD_PASSWORD, decide(record, "wrong").outcome());
        assertEquals(Outcome.EXPIRED, decide(record, "secret1").outcome());
        record.passwordExpiration = "not-a-date";
        assertEquals(Outcome.EXPIRED, decide(record, "secret1").outcome());
        record.passwordExpiration = NOW.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE);
        assertEquals(Outcome.SUCCESS, decide(record, "secret1").outcome());
    }

    @Test
    void successReturnsUserAndPasswordChangeRequirement() {
        UserRecord record = activeUser();
        record.forcePasswordChange = true;
        AuthenticationLogic.Result result = decide(record, "secret1").toResult("user1", record, null);
        assertTrue(result.authenticated());
        assertEquals("user1", result.user().getUserId());
        assertEquals("1", result.user().getPermission());
        assertTrue(result.forcePasswordChange());
        assertNull(result.errorCode());
        assertNull(result.message());
    }

    @Test
    void authenticationOutcomesMapToStableErrorCodes() {
        assertEquals("ERR00000001", Outcome.NO_USER.errorCode);
        assertEquals(Outcome.NO_USER.errorCode, Outcome.BAD_PASSWORD.errorCode);
        assertEquals("ERR00000002", Outcome.DISABLED.errorCode);
        assertEquals("ERR00000003", Outcome.AI_ACCOUNT.errorCode);
        assertEquals("ERR00000004", Outcome.ALREADY_LOCKED.errorCode);
        assertEquals("ERR00000005", Outcome.NOW_LOCKED.errorCode);
        assertEquals("ERR00000006", Outcome.EXPIRED.errorCode);
        assertNull(Outcome.SUCCESS.errorCode);
    }

    private Decision decide(UserRecord record, String password) {
        return AuthenticationLogic.evaluate(record, password, NOW, 5, 15);
    }

    private UserRecord activeUser() {
        UserRecord record = new UserRecord();
        record.password = PasswordUtil.encode("secret1");
        record.firstName = "太郎";
        record.lastName = "山田";
        record.permission = "1";
        record.accountType = "HUMAN";
        record.deleteFlag = "0";
        record.passwordExpiration = "99999999";
        return record;
    }
}
