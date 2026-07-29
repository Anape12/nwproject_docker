package jp.nw.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatRoomMemberEntity {
    
    private String roomId;
    private String userId;
    private LocalDateTime joinedAt;
}
