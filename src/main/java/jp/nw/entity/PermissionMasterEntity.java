package jp.nw.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionMasterEntity {

    private String permissionId;
    private String permissionName;
    private int displayOrder;
    private String deleteFlag;
}
