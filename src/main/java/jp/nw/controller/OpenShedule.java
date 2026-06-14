package jp.nw.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.nw.entity.UserEntity;
import jp.nw.model.MyCalendar;
import jp.nw.model.MyCalendarLogic;
import jp.nw.model.UserViewLogic;

/**
 * Servlet implementation class UserView
 */
@WebServlet("/OpenShedule")
public class OpenShedule extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	// public UserView() {
	// super();
	// // TODO Auto-generated constructor stub
	// }

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			// ユーザーID取得
			HttpSession session = request.getSession();
			UserEntity user = (UserEntity) session.getAttribute("loginUser");
			String userId = user.getUserId();

			String s_year = request.getParameter("year");
			String s_month = request.getParameter("month");
			// インスタンス生成に伴い、取得するユーザーＩＤを設定
			MyCalendarLogic logic = new MyCalendarLogic(userId);
			MyCalendar mc = null;
			if (s_year != null && s_month != null) {
				int year = Integer.parseInt(s_year);
				int month = Integer.parseInt(s_month);
				if (month == 0) {
					month = 12;
					year--;
				}
				if (month == 13) {
					month = 1;
					year++;
				}
				// 年と月のクエリパラメーターが来ている場合にはその年月でカレンダーを生成する
				mc = logic.createMyCalendar(year, month);
			} else {
				// クエリパラメータが来ていないときは実行日時のカレンダーを生成する。
				mc = logic.createMyCalendar();
			}
			// リクエストスコープに格納
			request.setAttribute("mc", mc);
			// viewにフォワード
			RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/jsp/Schedule/UserSchedule.jsp");
			rd.forward(request, response);
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html; charset=Shift_JIS");
		// 選択されたユーザーIDを取得
		String userId = request.getParameter("radiobutton");
		// ユーザー情報編集
		UserViewLogic userview = new UserViewLogic();
		List<UserEntity> userList = userview.editUserInfo(userId);
		request.setAttribute("userList", userList);

		HttpSession session = request.getSession();
		UserEntity loginUser = (UserEntity) session.getAttribute("loginUser");

		if (loginUser == null) {
			response.sendRedirect("/nwproject_B/");
			request.setCharacterEncoding("UTF-8");
			request.setAttribute("errorMsg", "エラー");
		} else {
			RequestDispatcher dispatcher = request.getRequestDispatcher("WEB-INF/jsp/editUserInfo.jsp");
			dispatcher.forward(request, response);
		}
	}

}
