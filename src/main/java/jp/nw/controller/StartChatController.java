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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jp.nw.application.RegistrationChatCommand;
import jp.nw.base.CommandData;
import jp.nw.entity.ChatMessageEntity;
import jp.nw.entity.UserEntity;

/**
 * Servlet implementation class UserView
 */
@WebServlet("/StartChat")
public class StartChatController extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final ObjectMapper JSON_MAPPER = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		RequestDispatcher dispatcher = request.getRequestDispatcher(
				"/WEB-INF/jsp/chat/ChatWindow.jsp");

		dispatcher.forward(request, response);

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		String comment = request.getParameter("commentText");
		String roomId = request.getParameter("roomId");

		HttpSession session = request.getSession();
		session.setAttribute("roomId", roomId);
		session.setAttribute("comment", comment);

		UserEntity userEntity = (UserEntity)session.getAttribute("loginUser");

		RegistrationChatCommand command = new RegistrationChatCommand();
		command.setCommandData(request, response);
		CommandData output = command.execute();

		List<ChatMessageEntity> chatMessageList = (List<ChatMessageEntity>) output.getValue("ChatMessageList");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		JSON_MAPPER.writeValue(response.getWriter(), chatMessageList);
	}

}
