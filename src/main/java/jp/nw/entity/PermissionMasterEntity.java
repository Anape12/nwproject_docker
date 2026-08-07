package jp.nw.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PermissionMasterEntity {

    private String permissionId;
    private String permissionName;
    private int displayOrder;
    private String deleteFlag;
}
