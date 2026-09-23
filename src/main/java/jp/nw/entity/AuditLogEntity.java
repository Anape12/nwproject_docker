package jp.nw.entity;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditLogEntity {
    private long auditId;
    private String eventCategory;
    private String eventAction;
    private String actorUserId;
    private String targetType;
    private String targetId;
    private boolean success;
    private String ipAddress;
    private String userAgent;
    private String detail;
    private LocalDateTime createdAt;
}

