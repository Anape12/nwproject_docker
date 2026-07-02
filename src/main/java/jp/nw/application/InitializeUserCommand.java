package jp.nw.application;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import jp.nw.base.ApplicationCommand;
import jp.nw.base.CommandData;
import jp.nw.parts.DBBase;
import jp.nw.parts.Query;
import jp.nw.parts.SqlType;

public class InitializeUserCommand extends ApplicationCommand {

    private HttpServletRequest request = null;
    private HttpServletResponse response = null;

    private DBBase dbCon = null;

    private Query query;

    private LinkedHashMap<String, Object> values;
    private LinkedHashMap<String, Object> conditions;

    private PasswordEncoder passwordEncoder;

    private boolean finshFlg = false;

    public boolean setCommandData(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        return true;
    }

    protected boolean doCommandData() {

        // 対象となるユーザーの存在チェック
        // 対象となるユーザーIDを取得
        String targetUserId = this.request.getParameter("radiobutton");
        this.conditions = new LinkedHashMap<>();
        this.conditions.put("user_id", targetUserId);

        // 対象ユーザーのパスワードを初期化更新(0000をハッシュ化)
        this.values = new LinkedHashMap<>();
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.values.put("password", this.passwordEncoder.encode("0000"));

        return true;
    }

    /**
     * SQL実行処理
     * 
     * @return boolean
     */
    protected boolean executeCommand() {

        // Query情報格納
        this.query = Query.builder()
                .sqlType(SqlType.UPDATE)
                .tableName("users_info")
                .values(this.values)
                .conditions(this.conditions)
                .build();

        this.dbCon = new DBBase();
        this.dbCon.execute(this.query);

        // 更新の完了判定
        this.query = Query.builder()
                .sqlType(SqlType.SELECT)
                .tableName("users_info")
                .selectColumns(List.of("user_id", "password"))
                .conditions(this.values)
                .build();

        this.dbCon = new DBBase();
        List<Object> resultList = (List<Object>) this.dbCon.execute(this.query);

        if (resultList.isEmpty()) {
            return false;
        }

        // 更新後のレコードのパスワードを取得
        String updatedPassword = (String) ((HashMap<String, Object>) resultList.get(0)).get("password");
        if (this.passwordEncoder.matches("0000", updatedPassword)) {
            this.finshFlg = true;
            return true;
        } else {
            this.finshFlg = false;
            return false;
        }
    }

    @Override
    protected boolean commandOutput() {
        this.output = new CommandData();
        this.output.setValue("finshFlg", this.finshFlg);
        return this.finshFlg;
    }
}
