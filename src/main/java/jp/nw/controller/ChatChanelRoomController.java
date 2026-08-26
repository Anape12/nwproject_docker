package jp.nw.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.nw.application.ChatRoomOpenCommand;
import jp.nw.base.CommandData;
import jp.nw.entity.ChatMessageEntity;

/**
 * Servlet implementation class UserView
 */
@WebServlet("/ChatChanelRoom")
public class ChatChanelRoomController extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// チャットを表示
		String roomId = request.getParameter("roomId");

		ChatRoomOpenCommand command = new ChatRoomOpenCommand();
		request.setAttribute("targetRoomId", roomId);
		command.setCommandData(request, response);

		CommandData output = command.execute();

		HttpSession session = request.getSession();
		session.setAttribute("chatDetail", (List<ChatMessageEntity>)output.getValue("chatDetail"));
		session.setAttribute("RoomName", (String)request.getParameter("displayName"));
		session.setAttribute("RoomId", roomId);
		request.setAttribute("attachments",new jp.nw.model.AttachmentLogic().find("CHAT",roomId));

		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/chat/ChatWindow.jsp");

		dispatcher.forward(request, response);

	}
}
