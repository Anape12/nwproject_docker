package jp.nw.application;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.nw.base.ApplicationCommand;
import jp.nw.entity.UserEntity;
import jp.nw.parts.Query;
import jp.nw.parts.SqlType;

public class InitializeUserCommand extends ApplicationCommand {

    private HttpServletRequest request = null;
    private HttpServletResponse response = null;

    private List<UserEntity> userList = null;

    public boolean setCommandData(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        return true;
    }

    protected boolean doCommandData() {

        // 対象となるユーザーの存在チェック
        // 対象となるユーザーIDを取得
        String targetUserId = this.request.getParameter("radiobutton");

        // 対象ユーザーのパスワードを初期化更新(0000をハッシュ化)

        // 最後に0000のハッシュコードと更新後のレコードを突合し、更新が成功しているか確認する

        // Query情報格納
        Query query = Query.builder()
                .sqlType(SqlType.UPDATE)
                .tableName("users_info")
                // .selectColumns(List.of(KEY_QERYNAME, KEY_USERPASS, KEY_USERPERMISS,
                // KEY_PASS_EXPIRATION))
                // .conditions(conditions)
                .build();

        return true;
    }

    protected boolean executeCommand() {

        return true;
    }

}
