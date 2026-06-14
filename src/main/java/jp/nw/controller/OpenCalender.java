package jp.nw.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import jp.nw.model.OpenCalenderLogic;

/**
 * Servlet implementation class UserView
 */
@WebServlet("/OpenCalender")
public class OpenCalender extends HttpServlet {

	private static final long serialVersionUID = 1L;

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
				// 対象のユーザーのスケジュールを取得
				String date = s_year.concat("-").concat(s_month);
				OpenCalenderLogic calender = new OpenCalenderLogic();
				calender.getSchedule(userId, date);

			} else {
				// クエリパラメータが来ていないときは実行日時のカレンダーを生成する。
				mc = logic.createMyCalendar();
			}

			// リクエストスコープに格納
			request.setAttribute("mc", mc);
			request.setAttribute("user", userId);
			// viewにフォワード
			RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/jsp/calender/calender.jsp");
			rd.forward(request, response);
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html; charset=Shift_JIS");
		// スケジュール月取得
		String param = request.getQueryString();

		String para[] = param.split("&");
		int[] stat = new int[2];
		// 当日日付対応
		if (para[1].contains("*")) {
			para[1] = para[1].replace("*", "");
		}
		stat[0] = Integer.parseInt(para[0]);
		stat[1] = Integer.parseInt(para[1]);

		request.setAttribute("month", stat[0]);
		request.setAttribute("day", stat[1]);
		request.setAttribute("user", para[2]);

		// カレンダー情報読み込み
		OpenCalenderLogic calender = new OpenCalenderLogic();
		Map<String, List<String>> map = calender.readCalender(para, stat);
		List<List<String>> list = new ArrayList<List<String>>();
		Integer q = 0;
		for (int i = 0; i < map.size(); i++) {
			String key = "KEY";
			key = key.concat(q.toString());
			list.add(map.get(key));
			q++;
		}
		StringBuilder sbVal = new StringBuilder();
		for (int j = 0; j < list.size(); j++) {
			List<String> ls = list.get(j);
			if (sbVal == null || sbVal.toString().equals("")) {
				sbVal.append(ls.get(1).concat("=").concat(ls.get(3)));
				sbVal.append("&");
			} else {
				sbVal.append(ls.get(1).concat("=").concat(ls.get(3)));
				sbVal.append("&");
			}
		}
		// 末尾削除
		String sendVal = sbVal.toString().replaceFirst(".$", "");
		request.setAttribute("SJVAL", sendVal);
		String[] vallist = sendVal.split("&");
		String[][] rest = new String[vallist.length][];
		for (int i = 0; i < vallist.length; i++) {
			rest[i] = vallist[i].split("=");
		}
		request.setAttribute("SCH", rest);
		RequestDispatcher dispatcher = request.getRequestDispatcher("WEB-INF/jsp/calender/schedule.jsp");
		dispatcher.forward(request, response);
	}

}
