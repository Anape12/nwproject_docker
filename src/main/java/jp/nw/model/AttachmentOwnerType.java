package jp.nw.model;

import jp.nw.model.CodedException;

import java.util.Locale;
import java.util.UUID;

public enum AttachmentOwnerType {
    REPORT,
    CHAT,
    SCHEDULE,
    APPROVAL;

    public static AttachmentOwnerType parse(String value) {
        if (value == null || value.isBlank()) throw new CodedException.Validation("ERR00010074");
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new CodedException.Validation("ERR00010074");
        }
    }

    public String validateOwnerId(String value) {
        if (value == null || value.isBlank() || value.length() > 64) {
            throw new CodedException.Validation("ERR00010075");
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
            throw new CodedException.Validation("ERR00010075");
        }
    }
}
