package jp.nw.controller;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.nw.entity.UserEntity;
import jp.nw.model.ScheduleEventLogic;

@WebServlet("/ScheduleResponse")
public class ScheduleResponseController extends HttpServlet {
    protected void doPost(HttpServletRequest q, HttpServletResponse s) throws IOException {
        UserEntity u = (UserEntity) q.getSession().getAttribute("loginUser");
        try {
            new ScheduleEventLogic().respond(Long.parseLong(q.getParameter("eventId")), u.getUserId(),
                    q.getParameter("status"));
        } catch (Exception e) {
            q.getSession().setAttribute("scheduleFlash", e.getMessage());
        }
        s.sendRedirect(q.getContextPath() + "/OpenCalender");
    }
}
