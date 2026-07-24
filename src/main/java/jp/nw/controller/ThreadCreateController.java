package jp.nw.controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.nw.application.ThreadCreateCommand;

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
		threadCommand.execute();

		// 登録後、スレッド詳細画面へ遷移
	}

}
