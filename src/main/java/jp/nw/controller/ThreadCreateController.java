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

import jp.nw.application.ThreadCreateCommand;
import jp.nw.base.CommandData;
import jp.nw.dao.ThreadDao;
import jp.nw.dto.ThreadCommentDto;
import jp.nw.dto.ThreadDto;
import jp.nw.entity.UserEntity;

/**
 * Servlet implementation class ThreadCreateController
 */
@WebServlet("/ThreadCreate")
public class ThreadCreateController extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		RequestDispatcher dispatcher = request.getRequestDispatcher(
				"/WEB-INF/jsp/thread/NewThread.jsp");

		dispatcher.forward(request, response);

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// スレッド内容を確定
		ThreadCreateCommand threadCommand = new ThreadCreateCommand();
		threadCommand.setCommandData(request, response);

		// スレッド情報登録コマンドを実行
		CommandData commnadOutput = threadCommand.execute();

		// 追加したスレッドのIDを取得
		commnadOutput.getValue("createId");

		// 登録後、スレッド詳細画面へ遷移
		int targetId = ((Long)commnadOutput.getValue("createId")).intValue();
		ThreadDao dao = new ThreadDao();
		ThreadDto thread = dao.findById(targetId);

		List<ThreadCommentDto> commentList = dao.findComments(targetId);

		// ログインユーザーのセット
		HttpSession session = request.getSession();
		UserEntity loginUser = (UserEntity) session.getAttribute(
						"loginUser");
		request.setAttribute(
						"loginUserId",
						loginUser.getUserId());

		request.setAttribute("thread", thread);
		request.setAttribute("commentList", commentList);

		RequestDispatcher dispatcher = request.getRequestDispatcher(
						"/WEB-INF/jsp/thread/ThreadDetail.jsp");

		dispatcher.forward(request, response);
	}

}
