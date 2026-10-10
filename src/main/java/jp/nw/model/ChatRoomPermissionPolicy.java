package jp.nw.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jp.nw.domain.user.PermissionAction;
import jp.nw.domain.user.PermissionStatus;
import jp.nw.entity.UserEntity;
import jp.nw.parts.DBBase;
import jp.nw.util.PermissionCheckUtil;

/** Owner or business administrator may manage an active group chat. */
public class ChatRoomPermissionPolicy {
    public boolean canManageMembers(UserEntity user, String roomId) {
        if (user == null || user.getUserId() == null || roomId == null
                || "AI".equalsIgnoreCase(user.getAccountType())
                || PermissionStatus.fromValue(user.getPermission()).isEmpty()) return false;
        DBBase db = new DBBase();
        try (Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT created_by_id FROM chat_room WHERE room_id=? AND room_type='2' AND delete_flg='0'")) {
            statement.setString(1, roomId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() && (user.getUserId().equals(result.getString(1))
                        || PermissionCheckUtil.can(user, PermissionAction.CHAT_MEMBERS_MANAGE_ANY));
            }
        } catch (SQLException error) {
            return false;
        }
    }
}
