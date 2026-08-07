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
public class ChatRoomMemberEntity {
    
    private String roomId;
    private String userId;
    private LocalDateTime joinedAt;
}
