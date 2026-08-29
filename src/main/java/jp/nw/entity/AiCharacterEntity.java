package jp.nw.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AiCharacterEntity {
    private long characterId;
    private String userId;
    private String characterName;
    private String promptKey;
    private String modelName;
    private String replyMode;
    private String activeFlg;
}
