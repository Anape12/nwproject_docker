package jp.nw.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jp.nw.entity.UserEntity;
import jp.nw.model.ChatOpenLogic;
import jp.nw.parts.DBBase;

@WebServlet("/ChatMessages")
public class ChatMessagesController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final ObjectMapper JSON = new ObjectMapper().registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    protected void doGet(HttpServletRequest q, HttpServletResponse s) throws IOException {
        UserEntity u = (UserEntity) q.getSession().getAttribute("loginUser");
        String room = q.getParameter("roomId");
        if (!member(u.getUserId(), room)) {
            s.sendError(403);
            return;
        }
        s.setContentType("application/json");
        s.setCharacterEncoding("UTF-8");
        JSON.writeValue(s.getWriter(), new ChatOpenLogic().getChanelOpen(room, q));
    }

    private boolean member(String user, String room) {
        DBBase db = new DBBase();
        try (Connection c = db.getConnection();
                PreparedStatement p = c
                        .prepareStatement("SELECT 1 FROM chat_room_member WHERE room_id=? AND user_id=?")) {
            p.setString(1, room);
            p.setString(2, user);
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        } catch (Exception e) {
            return false;
        }
    }
}
