package jp.nw.controller;

import java.io.IOException;
import java.util.LinkedHashMap;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.nw.entity.UserEntity;
import jp.nw.parts.DBBase;
import jp.nw.parts.Query;
import jp.nw.parts.SqlType;

@WebServlet("/Logout")
public class LogoutController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        values.put("current_login_token", null);
        LinkedHashMap<String, Object> conditions = new LinkedHashMap<>();
        UserEntity loginUser = (UserEntity) session.getAttribute("loginUser");
        conditions.put("user_id", loginUser.getUserId());

        Query query = Query.builder()
                .sqlType(SqlType.UPDATE)
                .tableName("users_info")
                .values(values)
                .conditions(conditions)
                .build();

        DBBase db = new DBBase();
        db.execute(query);

        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect(request.getContextPath() + "/Login");
    }
}
