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

import jp.nw.application.ChatChanelListCommand;
import jp.nw.base.CommandData;
import jp.nw.entity.ChatRoomEntity;

/**
 * Servlet implementation class UserView
 */
@WebServlet("/ChatChanelList")
public class ChatChanelListController extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 自分自身が参照できるChatチャンネルを表示
		ChatChanelListCommand command = new ChatChanelListCommand();
		command.setCommandData(request, response);
		CommandData output = command.execute();

		List<ChatRoomEntity> targetChatRoom = (List<ChatRoomEntity>) output.getValue("chatchanels");
		HttpSession session = request.getSession();
		session.setAttribute("chatRooms", targetChatRoom);

		RequestDispatcher dispatcher = request.getRequestDispatcher(
				"/WEB-INF/jsp/chat/ChatList.jsp");

		dispatcher.forward(request, response);

	}
}
