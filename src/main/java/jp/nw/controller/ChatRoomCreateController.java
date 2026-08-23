package jp.nw.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.nw.entity.UserEntity;
import jp.nw.model.ChatRoomCreateLogic;
import jp.nw.model.ChatRoomCreateLogic.CreatedRoom;

@WebServlet("/ChatRoomCreate")
public class ChatRoomCreateController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserEntity loginUser = getLoginUser(request);
        ChatRoomCreateLogic logic = new ChatRoomCreateLogic();
        request.setAttribute("chatUsers", logic.getAvailableUsers(loginUser.getUserId()));

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/chat/ChatRoomCreate.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        UserEntity loginUser = getLoginUser(request);
        ChatRoomCreateLogic logic = new ChatRoomCreateLogic();

        try {
            CreatedRoom room = logic.createOrGetDirectRoom(
                    loginUser.getUserId(), request.getParameter("targetUserId"));
            String displayName = URLEncoder.encode(room.displayName(), StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/ChatChanelRoom?roomId="
                    + room.roomId() + "&displayName=" + displayName);
        } catch (IllegalArgumentException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.setAttribute("chatUsers", logic.getAvailableUsers(loginUser.getUserId()));
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.getRequestDispatcher("/WEB-INF/jsp/chat/ChatRoomCreate.jsp").forward(request, response);
        }
    }

    private UserEntity getLoginUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (UserEntity) session.getAttribute("loginUser");
    }
}
