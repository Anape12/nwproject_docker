package jp.nw.domain.user;

import java.util.Arrays;
import java.util.Optional;

public enum PermissionStatus {

    SYSTEM_ADMINISTRATOR("0"),
    ADMINISTRATOR("1"),
    GENERAL("2");

    private final String value;

    PermissionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<PermissionStatus> fromValue(String value) {
        return Arrays.stream(values()).filter(role -> role.value.equals(value)).findFirst();
    }
}
