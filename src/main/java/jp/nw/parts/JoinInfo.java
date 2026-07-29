package jp.nw.parts;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JoinInfo {
    private JoinType joinType;
    private String tableName;
    private String condition;
}
