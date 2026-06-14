package jp.nw.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
import jp.nw.model.OpenCalenderLogic;
import jp.nw.parts.KeyParts;

/**
 * Servlet implementation class UserView
 */
@WebServlet("/WorkManagement")
public class WorkManagement extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			// ユーザーID取得
			HttpSession session = request.getSession();
			UserEntity user = (UserEntity) session.getAttribute("loginUser");
			String userId = user.getUserId();
			// ユーザーIDを設定
			request.setAttribute(KeyParts.GET_KEY.USER_ID, userId);
			// 取得月を設定
			Calendar cal = Calendar.getInstance();
			int month = cal.get(Calendar.MONTH);
			// 現在月を取得
			if (month++ > 12) {
				month = 1;
			}
			request.setAttribute(KeyParts.GET_KEY.GET_MONTH, month);

			Calendar today = Calendar.getInstance();
			Calendar firstDay = Calendar.getInstance();
			firstDay.set(Calendar.DATE, 1);
			int firstDate = firstDay.get(Calendar.DAY_OF_WEEK);
			request.setAttribute(KeyParts.GET_KEY.GET_WEEK, getWeek());
			request.setAttribute(KeyParts.GET_KEY.FIRST_DATE, firstDate);

			// viewにフォワード
			RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/jsp/WorkManagement/workmanegiment.jsp");
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

	private Map<Integer, String> getWeek() {

		Map<Integer, String> week = new HashMap<Integer, String>();
		week.put(1, "日");
		week.put(2, "月");
		week.put(3, "火");
		week.put(4, "水");
		week.put(5, "木");
		week.put(6, "金");
		week.put(7, "土");

		return week;
	}
}
