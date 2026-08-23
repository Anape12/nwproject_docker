package jp.nw.controller;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.nw.entity.ScheduleEventEntity;
import jp.nw.entity.UserEntity;
import jp.nw.model.ScheduleCalendarDay;
import jp.nw.model.ScheduleEventLogic;

@WebServlet("/OpenCalender")
public class OpenCalender extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Set<String> COLORS = Set.of("#1a73e8", "#188038", "#d93025", "#9334e6", "#f9ab00", "#5f6368");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserEntity loginUser = (UserEntity) session.getAttribute("loginUser");
        YearMonth month = parseMonth(request);
        LocalDate selectedDate = parseDate(request.getParameter("date"), LocalDate.now());
        ScheduleEventLogic logic = new ScheduleEventLogic();

        LocalDate gridStart = previousOrSameSunday(month.atDay(1));
        LocalDate gridEnd = nextOrSameSaturday(month.atEndOfMonth());
        List<ScheduleEventEntity> events = logic.findInRange(loginUser.getUserId(), gridStart.atStartOfDay(), gridEnd.plusDays(1).atStartOfDay());
        ScheduleEventEntity editEvent = findEditEvent(request.getParameter("edit"), loginUser.getUserId(), logic);

        String csrfToken = (String) session.getAttribute("scheduleCsrfToken");
        if (csrfToken == null) {
            csrfToken = UUID.randomUUID().toString();
            session.setAttribute("scheduleCsrfToken", csrfToken);
        }

        request.setAttribute("calendarDays", createDays(month, gridStart, gridEnd, events));
        request.setAttribute("displayMonth", month);
        request.setAttribute("previousMonth", month.minusMonths(1));
        request.setAttribute("nextMonth", month.plusMonths(1));
        request.setAttribute("selectedDate", selectedDate);
        request.setAttribute("editEvent", editEvent);
        request.setAttribute("flashMessage", session.getAttribute("scheduleFlash"));
        request.setAttribute("flashType", session.getAttribute("scheduleFlashType"));
        request.setAttribute("csrfToken", csrfToken);
        session.removeAttribute("scheduleFlash");
        session.removeAttribute("scheduleFlashType");
        request.getRequestDispatcher("/WEB-INF/jsp/calender/calender.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        UserEntity loginUser = (UserEntity) session.getAttribute("loginUser");
        YearMonth month = parseMonth(request);

        if (!Objects.equals(session.getAttribute("scheduleCsrfToken"), request.getParameter("csrfToken"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "不正なリクエストです。");
            return;
        }

        ScheduleEventLogic logic = new ScheduleEventLogic();
        try {
            String action = request.getParameter("action");
            if ("delete".equals(action)) {
                if (!logic.delete(parseRequiredId(request.getParameter("eventId")), loginUser.getUserId())) {
                    throw new IllegalArgumentException("削除対象の予定が見つかりません。");
                }
                setFlash(session, "予定を削除しました。", "success");
            } else {
                ScheduleEventEntity event = buildEvent(request, loginUser.getUserId());
                if ("update".equals(action)) {
                    event.setEventId(parseRequiredId(request.getParameter("eventId")));
                    if (!logic.update(event)) throw new IllegalArgumentException("更新対象の予定が見つかりません。");
                    setFlash(session, "予定を更新しました。", "success");
                } else {
                    logic.create(event);
                    setFlash(session, "予定を登録しました。", "success");
                }
            }
        } catch (IllegalArgumentException | DateTimeParseException e) {
            setFlash(session, e.getMessage() == null ? "入力内容を確認してください。" : e.getMessage(), "error");
        }
        response.sendRedirect(request.getContextPath() + "/OpenCalender?year=" + month.getYear() + "&month=" + month.getMonthValue());
    }

    private ScheduleEventEntity buildEvent(HttpServletRequest request, String userId) {
        String title = trim(request.getParameter("title"));
        String description = trim(request.getParameter("description"));
        boolean allDay = "true".equals(request.getParameter("allDay"));
        LocalDate startDate = LocalDate.parse(request.getParameter("startDate"));
        LocalDate endDate = LocalDate.parse(request.getParameter("endDate"));
        if (title.isBlank() || title.length() > 100) throw new IllegalArgumentException("タイトルを1～100文字で入力してください。");
        if (description.length() > 1000) throw new IllegalArgumentException("詳細は1000文字以内で入力してください。");
        if (endDate.isBefore(startDate)) throw new IllegalArgumentException("終了日は開始日以降にしてください。");

        LocalDateTime startAt;
        LocalDateTime endAt;
        if (allDay) {
            startAt = startDate.atStartOfDay();
            endAt = endDate.plusDays(1).atStartOfDay();
        } else {
            startAt = LocalDateTime.of(startDate, LocalTime.parse(request.getParameter("startTime")));
            endAt = LocalDateTime.of(endDate, LocalTime.parse(request.getParameter("endTime")));
            if (!endAt.isAfter(startAt)) throw new IllegalArgumentException("終了日時は開始日時より後にしてください。");
        }
        String color = request.getParameter("color");
        if (!COLORS.contains(color)) color = "#1a73e8";
        return ScheduleEventEntity.builder().userId(userId).title(title).description(description)
                .startAt(startAt).endAt(endAt).allDay(allDay).color(color).build();
    }

    private List<ScheduleCalendarDay> createDays(YearMonth month, LocalDate start, LocalDate end, List<ScheduleEventEntity> events) {
        List<ScheduleCalendarDay> days = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            ScheduleCalendarDay day = new ScheduleCalendarDay(date, YearMonth.from(date).equals(month), date.equals(LocalDate.now()));
            for (ScheduleEventEntity event : events) {
                if (!date.isBefore(event.getStartAt().toLocalDate()) && !date.isAfter(event.getLastDisplayDate())) day.getEvents().add(event);
            }
            days.add(day);
        }
        return days;
    }

    private LocalDate previousOrSameSunday(LocalDate date) {
        while (date.getDayOfWeek() != DayOfWeek.SUNDAY) date = date.minusDays(1);
        return date;
    }

    private LocalDate nextOrSameSaturday(LocalDate date) {
        while (date.getDayOfWeek() != DayOfWeek.SATURDAY) date = date.plusDays(1);
        return date;
    }

    private ScheduleEventEntity findEditEvent(String value, String userId, ScheduleEventLogic logic) {
        if (value == null || value.isBlank()) return null;
        try { return logic.findById(Long.parseLong(value), userId); } catch (NumberFormatException e) { return null; }
    }

    private long parseRequiredId(String value) {
        try { return Long.parseLong(value); } catch (NumberFormatException e) { throw new IllegalArgumentException("予定IDが不正です。"); }
    }

    private YearMonth parseMonth(HttpServletRequest request) {
        try {
            String year = request.getParameter("year"); String month = request.getParameter("month");
            return year == null || month == null ? YearMonth.now() : YearMonth.of(Integer.parseInt(year), Integer.parseInt(month));
        } catch (RuntimeException e) { return YearMonth.now(); }
    }

    private LocalDate parseDate(String value, LocalDate fallback) {
        try { return value == null ? fallback : LocalDate.parse(value); } catch (DateTimeParseException e) { return fallback; }
    }

    private String trim(String value) { return value == null ? "" : value.trim(); }
    private void setFlash(HttpSession session, String message, String type) {
        session.setAttribute("scheduleFlash", message); session.setAttribute("scheduleFlashType", type);
    }
}
