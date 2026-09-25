package jp.nw.util;

import jp.nw.domain.user.PermissionStatus;

public class StatusCheckUtil {

    public static boolean isAdministrator(String permisstion) {
        return PermissionStatus.ADMINISTRATOR.getValue().equals(permisstion);
    }
}
