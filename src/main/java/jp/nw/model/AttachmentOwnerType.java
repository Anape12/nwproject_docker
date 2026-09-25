package jp.nw.model;

import java.util.Locale;
import java.util.UUID;

public enum AttachmentOwnerType {
    REPORT,
    CHAT,
    SCHEDULE,
    APPROVAL;

    public static AttachmentOwnerType parse(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("添付先種別が不正です。");
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("添付先種別が不正です。");
        }
    }

    public String validateOwnerId(String value) {
        if (value == null || value.isBlank() || value.length() > 64) {
            throw new IllegalArgumentException("添付先IDが不正です。");
        }
        String normalized = value.trim();
        try {
            if (this == CHAT) {
                UUID.fromString(normalized);
            } else {
                long id = Long.parseLong(normalized);
                if (id <= 0) throw new NumberFormatException();
            }
            return normalized;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("添付先IDが不正です。");
        }
    }
}
