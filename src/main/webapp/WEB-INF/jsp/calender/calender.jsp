<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>${displayMonth.year}年${displayMonth.monthValue}月 スケジュール</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/schedule.css">
</head>
<body>
  <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  <main class="schedule-page">
    <section class="calendar-panel">
      <div class="calendar-toolbar">
        <a class="nav-button" href="${pageContext.request.contextPath}/OpenCalender?year=${previousMonth.year}&amp;month=${previousMonth.monthValue}">‹</a>
        <a class="today-button" href="${pageContext.request.contextPath}/OpenCalender">今日</a>
        <a class="nav-button" href="${pageContext.request.contextPath}/OpenCalender?year=${nextMonth.year}&amp;month=${nextMonth.monthValue}">›</a>
        <h1>${displayMonth.year}年 ${displayMonth.monthValue}月</h1>
      </div>
      <c:if test="${not empty flashMessage}"><div class="flash ${flashType}"><c:out value="${flashMessage}"/></div></c:if>
      <div class="calendar-grid weekday-row">
        <div class="sunday">日</div><div>月</div><div>火</div><div>水</div><div>木</div><div>金</div><div class="saturday">土</div>
      </div>
      <div class="calendar-grid month-grid">
        <c:forEach var="day" items="${calendarDays}">
          <div class="calendar-day ${day.currentMonth ? '' : 'outside'} ${day.today ? 'today' : ''}">
            <a class="day-number" href="${pageContext.request.contextPath}/OpenCalender?year=${displayMonth.year}&amp;month=${displayMonth.monthValue}&amp;date=${day.dateValue}">${day.dayOfMonth}</a>
            <div class="day-events">
              <c:forEach var="event" items="${day.events}">
                <a class="event-chip" style="--event-color:${event.color}"
                   href="${pageContext.request.contextPath}/OpenCalender?year=${displayMonth.year}&amp;month=${displayMonth.monthValue}&amp;edit=${event.eventId}">
                  <span class="event-time"><c:out value="${event.timeLabel}"/></span><span><c:out value="${event.title}"/></span>
                </a>
              </c:forEach>
            </div>
          </div>
        </c:forEach>
      </div>
    </section>

    <aside class="event-panel">
      <h2>${empty editEvent ? '予定を追加' : '予定を編集'}</h2>
      <form method="post" action="${pageContext.request.contextPath}/OpenCalender" id="event-form">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="year" value="${displayMonth.year}"><input type="hidden" name="month" value="${displayMonth.monthValue}">
        <input type="hidden" name="action" value="${empty editEvent ? 'create' : 'update'}">
        <c:if test="${not empty editEvent}"><input type="hidden" name="eventId" value="${editEvent.eventId}"></c:if>
        <label>タイトル <span class="required">必須</span>
          <input type="text" name="title" maxlength="100" required value="<c:out value='${editEvent.title}'/>">
        </label>
        <label class="all-day-row"><input type="checkbox" id="allDay" name="allDay" value="true" ${editEvent.allDay ? 'checked' : ''}> 終日</label>
        <div class="date-row">
          <label>開始日<input type="date" name="startDate" required value="${empty editEvent ? selectedDate : editEvent.startDateValue}"></label>
          <label>終了日<input type="date" name="endDate" required value="${empty editEvent ? selectedDate : editEvent.endDateValue}"></label>
        </div>
        <div class="date-row time-fields">
          <label>開始時刻<input type="time" name="startTime" value="${empty editEvent ? '09:00' : editEvent.startTimeValue}"></label>
          <label>終了時刻<input type="time" name="endTime" value="${empty editEvent ? '10:00' : editEvent.endTimeValue}"></label>
        </div>
        <label>色
          <select name="color">
            <option value="#1a73e8" ${empty editEvent || editEvent.color == '#1a73e8' ? 'selected' : ''}>ブルー</option>
            <option value="#188038" ${editEvent.color == '#188038' ? 'selected' : ''}>グリーン</option>
            <option value="#d93025" ${editEvent.color == '#d93025' ? 'selected' : ''}>レッド</option>
            <option value="#9334e6" ${editEvent.color == '#9334e6' ? 'selected' : ''}>パープル</option>
            <option value="#f9ab00" ${editEvent.color == '#f9ab00' ? 'selected' : ''}>オレンジ</option>
            <option value="#5f6368" ${editEvent.color == '#5f6368' ? 'selected' : ''}>グレー</option>
          </select>
        </label>
        <label>詳細<textarea name="description" maxlength="1000" rows="5"><c:out value="${editEvent.description}"/></textarea></label>
        <div class="form-actions"><button class="primary-button" type="submit">${empty editEvent ? '登録' : '更新'}</button>
          <c:if test="${not empty editEvent}"><a class="cancel-button" href="${pageContext.request.contextPath}/OpenCalender?year=${displayMonth.year}&amp;month=${displayMonth.monthValue}">キャンセル</a></c:if>
        </div>
      </form>
      <c:if test="${not empty editEvent}">
        <form method="post" action="${pageContext.request.contextPath}/OpenCalender" onsubmit="return confirm('この予定を削除しますか？');">
          <input type="hidden" name="csrfToken" value="${csrfToken}"><input type="hidden" name="year" value="${displayMonth.year}"><input type="hidden" name="month" value="${displayMonth.monthValue}">
          <input type="hidden" name="action" value="delete"><input type="hidden" name="eventId" value="${editEvent.eventId}">
          <button class="delete-button" type="submit">予定を削除</button>
        </form>
      </c:if>
    </aside>
  </main>
  <script>
    const allDay = document.getElementById('allDay');
    const timeFields = document.querySelector('.time-fields');
    function updateTimeFields(){timeFields.classList.toggle('disabled',allDay.checked);timeFields.querySelectorAll('input').forEach(input=>input.required=!allDay.checked);}
    allDay.addEventListener('change',updateTimeFields);updateTimeFields();
  </script>
</body>
</html>
