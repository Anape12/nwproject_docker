package jp.nw.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatRoomEntity {
    
    private String roomId;
    private String roomName;
    private String roomType;
    private String createdById;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String deleteFlg;
    private String displayName;
}
