package jp.nw.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.nw.model.WriteSheduleLogic;

/**
 * Servlet implementation class UserView
 */
@WebServlet("/WriteShedule")
public class WriteShedule extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public WriteShedule() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			// String s_year=request.getParameter("YEAR");
			// String s_month=request.getParameter("MONTH");
			// String s_day=request.getParameter("DAY");
			//
			// viewにフォワード
			RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/jsp/Schedule/UserSchedule.jsp");
			rd.forward(request, response);
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("UTF-8");
		request.setCharacterEncoding("UTF-8");
		// スケジュールの取得
		String s_year = request.getParameter("progyear");
		String s_month = request.getParameter("progmonth");
		String s_day = request.getParameter("progday");

		String fromhour = request.getParameter("fromhour");
		String fromminut = request.getParameter("fromminut");
		String tohour = request.getParameter("tohour");
		String tominit = request.getParameter("tominit");

		String plan = request.getParameter("plan");
		String memo = request.getParameter("memo");

		// スケジュール情報格納
		Map<String, String> sheduleInfo = new HashMap<String, String>();
		sheduleInfo.put("year", s_year);
		sheduleInfo.put("month", s_month);
		sheduleInfo.put("day", s_day);
		sheduleInfo.put("fromhour", fromhour);
		sheduleInfo.put("fromminit", fromminut);
		sheduleInfo.put("tohour", tohour);
		sheduleInfo.put("tominit", tominit);
		sheduleInfo.put("plan", plan);
		sheduleInfo.put("memo", memo);

		sheduleInfo.put("query", request.getQueryString());

		WriteSheduleLogic write = new WriteSheduleLogic();
		int result = write.writeSchedule(sheduleInfo);
		if (result == 1) {
			// 正常終了
			RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/jsp/Schedule/result.jsp");
			rd.forward(request, response);
		}
	}
}
