<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.time.Year,java.util.ArrayList,java.util.LinkedHashMap,java.util.List,java.util.Map" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% int month = (int) request.getAttribute("month"); int day = (int) request.getAttribute("day"); String userId =
(String) request.getAttribute("user"); String[][] schedules = (String[][]) request.getAttribute("SCH"); Map<String
    ,
    String
>
    plansByTime = new LinkedHashMap<>(); if (schedules != null) { for (String[] schedule : schedules) { if (schedule !=
    null && schedule.length >= 2) plansByTime.put(schedule[0], schedule[1]); } } List<map <String, String
        >> hourlySchedules = new ArrayList<>(); for (int hour = 0; hour < 24; hour++) { String key =
        String.format("%02d00", hour); Map<String , String>
            row = new LinkedHashMap<>(); row.put("time", String.format("%02d:00", hour)); row.put("plan",
            plansByTime.getOrDefault(key, "")); hourlySchedules.add(row); } List<Integer>
                years = new ArrayList<>(); for (int value = Year.now().getValue(); value < Year.now().getValue() + 10;
                value++) years.add(value); List<Integer>
                    months = new ArrayList<>(); for (int value = 1; value <= 12; value++) months.add(value);
                    List<Integer>
                        days = new ArrayList<>(); for (int value = 1; value <= 31; value++) days.add(value);
                        List<Integer>
                            hours = new ArrayList<>(); for (int value = 0; value < 24; value++) hours.add(value);
                            request.setAttribute("hourlySchedules", hourlySchedules);
                            request.setAttribute("scheduleYears", years); request.setAttribute("scheduleMonths",
                            months); request.setAttribute("scheduleDays", days); request.setAttribute("scheduleHours",
                            hours); request.setAttribute("selectedMonth", month); request.setAttribute("selectedDay",
                            day); %>
                            <!DOCTYPE html>
                            <html lang="ja">
                                <head>
                                    <meta charset="UTF-8" />
                                    <meta name="viewport" content="width=device-width, initial-scale=1" />
                                    <title>${selectedMonth}月カレンダー</title>
                                    <style>
                                        table.sche {
                                            border: 1px solid #a9a9a9;
                                            padding: 0;
                                            margin: 0;
                                            border-collapse: collapse;
                                        }
                                        td {
                                            vertical-align: top;
                                            margin: 0;
                                            padding: 2px;
                                            font-size: 0.75em;
                                            height: 20px;
                                        }
                                        td.top {
                                            border-bottom: 1px solid #a9a9a9;
                                            text-align: center;
                                        }
                                        td.time {
                                            background-color: #f0f8ff;
                                            text-align: right;
                                            border-right: 1px double #a9a9a9;
                                            padding-right: 5px;
                                        }
                                        td.timeb {
                                            background-color: #f0f8ff;
                                            border-bottom: 1px solid #a9a9a9;
                                            border-right: 1px double #a9a9a9;
                                        }
                                        td.contents {
                                            background-color: #fff;
                                            border-bottom: 1px dotted #a9a9a9;
                                        }
                                        td.contentsb {
                                            background-color: #fff;
                                            border-bottom: 1px solid #a9a9a9;
                                        }
                                        #contents {
                                            margin: 0;
                                            padding: 0;
                                            width: 710px;
                                        }
                                        #left {
                                            margin: 0;
                                            padding: 0;
                                            float: left;
                                            width: 400px;
                                        }
                                        #right {
                                            margin: 0;
                                            padding: 0;
                                            float: right;
                                            width: 300px;
                                            background-color: #fff;
                                        }
                                        #contents::after {
                                            content: "";
                                            display: block;
                                            clear: both;
                                        }
                                    </style>
                                </head>
                                <body>
                                    <form
                                        action="${pageContext.request.contextPath}/WriteShedule?<%= userId %>"
                                        method="post"
                                    >
                                        <p>スケジュール登録</p>
                                        <div id="contents">
                                            <div id="left">
                                                <table class="sche">
                                                    <thead>
                                                        <tr>
                                                            <th class="top" style="width: 80px">時刻</th>
                                                            <th class="top" style="width: 300px">予定</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <c:forEach var="row" items="${hourlySchedules}">
                                                            <tr>
                                                                <td class="time">${row.time}</td>
                                                                <td class="contents"><c:out value="${row.plan}" /></td>
                                                            </tr>
                                                            <tr>
                                                                <td class="timeb"></td>
                                                                <td class="contentsb"></td>
                                                            </tr>
                                                        </c:forEach>
                                                    </tbody>
                                                </table>
                                            </div>
                                            <div id="right">
                                                <table>
                                                    <tr>
                                                        <th>日付</th>
                                                        <td>
                                                            <select name="progyear">
                                                                <c:forEach var="year" items="${scheduleYears}"
                                                                    ><option value="${year}">${year}</option></c:forEach
                                                                >
                                                            </select>
                                                            <select name="progmonth">
                                                                <c:forEach var="value" items="${scheduleMonths}"
                                                                    ><option
                                                                        value="${value}"
                                                                        data-selected="${selectedMonth == value}"
                                                                    >
                                                                        ${value}
                                                                    </option></c:forEach
                                                                >
                                                            </select>
                                                            <select name="progday">
                                                                <c:forEach var="value" items="${scheduleDays}"
                                                                    ><option
                                                                        value="${value}"
                                                                        data-selected="${selectedDay == value}"
                                                                    >
                                                                        ${value}
                                                                    </option></c:forEach
                                                                >
                                                            </select>
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <th>時刻</th>
                                                        <td>
                                                            <select name="fromhour">
                                                                <option value="">--</option>
                                                                <c:forEach var="hour" items="${scheduleHours}"
                                                                    ><option value="${hour}">${hour}</option></c:forEach
                                                                >
                                                            </select>
                                                            <select name="fromminut">
                                                                <option value="">--</option>
                                                                <option value="00">00</option>
                                                                <option value="15">15</option>
                                                                <option value="30">30</option>
                                                                <option value="45">45</option>
                                                            </select>
                                                            ～
                                                            <select name="tohour">
                                                                <option value="">--</option>
                                                                <c:forEach var="hour" items="${scheduleHours}"
                                                                    ><option value="${hour}">${hour}</option></c:forEach
                                                                >
                                                            </select>
                                                            <select name="tominit">
                                                                <option value="">--</option>
                                                                <option value="00">00</option>
                                                                <option value="15">15</option>
                                                                <option value="30">30</option>
                                                                <option value="45">45</option>
                                                            </select>
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <th>予定</th>
                                                        <td>
                                                            <input type="text" name="plan" size="30" maxlength="100" />
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <th>メモ</th>
                                                        <td><textarea name="memo" cols="30" rows="10"></textarea></td>
                                                    </tr>
                                                </table>
                                                <p><button type="submit" name="Register">登録する</button></p>
                                            </div>
                                        </div>
                                    </form>
                                    <script src="${pageContext.request.contextPath}/js/jsp-boolean-attributes.js"></script>
                                </body>
                            </html> </Integer></Integer></Integer></Integer></String></map
></String>
