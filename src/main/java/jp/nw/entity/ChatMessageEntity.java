package jp.nw.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMessageEntity {
    
    private int messageId;
    private String roomId;
    private String postedById;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String deleteFlg;

    private String postedByName;

    private String chatTarget;
    
    private UserEntity userEntity;
}
