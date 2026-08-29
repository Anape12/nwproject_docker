package jp.nw.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
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
    private String postedByAccountType;

    private String chatTarget;
    
    private UserEntity userEntity;
}
