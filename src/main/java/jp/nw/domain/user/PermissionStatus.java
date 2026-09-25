package jp.nw.domain.user;

public enum PermissionStatus {

    ADMINISTRATOR("1"),
    GENERAL("2");

    private final String value;

    PermissionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
