package jp.nw.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.nw.entity.AttendanceEntity;
import jp.nw.entity.UserEntity;
import jp.nw.entity.WorkReportEntity;
import jp.nw.model.AttendanceDay;
import jp.nw.model.AttendanceLogic;
import jp.nw.model.WorkReportLogic;

@WebServlet("/WorkManagement")
public class WorkManagement extends HttpServlet {
    private static final long serialVersionUID=1L;
    private static final Set<String> WORK_TYPES=Set.of("OFFICE","REMOTE","LEAVE","HOLIDAY");
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        HttpSession session=request.getSession();UserEntity user=(UserEntity)session.getAttribute("loginUser");YearMonth month=month(request);AttendanceLogic logic=new AttendanceLogic();
        List<AttendanceEntity> records=logic.findMonth(user.getUserId(),month);Map<LocalDate,AttendanceEntity> byDate=new HashMap<>();for(AttendanceEntity record:records)byDate.put(record.getWorkDate(),record);
        List<AttendanceDay> days=new ArrayList<>();for(int day=1;day<=month.lengthOfMonth();day++){LocalDate date=month.atDay(day);days.add(new AttendanceDay(date,byDate.get(date)));}
        AttendanceEntity selected=selected(request.getParameter("edit"),user.getUserId(),logic);LocalDate selectedDate=parseDate(request.getParameter("date"),LocalDate.now());
        String token=(String)session.getAttribute("attendanceCsrfToken");if(token==null){token=UUID.randomUUID().toString();session.setAttribute("attendanceCsrfToken",token);}
        request.setAttribute("attendanceDays",days);request.setAttribute("displayMonth",month);request.setAttribute("previousMonth",month.minusMonths(1));request.setAttribute("nextMonth",month.plusMonths(1));request.setAttribute("selected",selected);request.setAttribute("selectedDate",selectedDate);request.setAttribute("reports",new WorkReportLogic().findOwn(user.getUserId()));request.setAttribute("csrfToken",token);
        request.setAttribute("flashMessage",session.getAttribute("attendanceFlash"));request.setAttribute("flashType",session.getAttribute("attendanceFlashType"));session.removeAttribute("attendanceFlash");session.removeAttribute("attendanceFlashType");
        request.getRequestDispatcher("/WEB-INF/jsp/WorkManagement/workmanegiment.jsp").forward(request,response);
    }
    @Override protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        request.setCharacterEncoding("UTF-8");HttpSession session=request.getSession(false);UserEntity user=(UserEntity)session.getAttribute("loginUser");YearMonth month=month(request);
        if(!Objects.equals(session.getAttribute("attendanceCsrfToken"),request.getParameter("csrfToken"))){response.sendError(403,"不正なリクエストです。");return;}
        AttendanceLogic logic=new AttendanceLogic();try{
            if("submitMonth".equals(request.getParameter("action"))){int count=new jp.nw.model.ApprovalLogic().submitAttendanceMonth(user.getUserId(),month);flash(session,month.getMonthValue()+"月の勤怠を"+count+"件一括申請しました。","success");}
            else if("submit".equals(request.getParameter("action"))){new jp.nw.model.ApprovalLogic().submit("ATTENDANCE",id(request),user.getUserId());flash(session,"勤怠を日付単位で承認申請しました。","success");}
            else if("delete".equals(request.getParameter("action"))){if(!logic.delete(id(request),user.getUserId()))throw new IllegalArgumentException("削除対象の勤怠情報が見つかりません。");flash(session,"勤怠情報を削除しました。","success");}
            else{AttendanceEntity value=build(request,user.getUserId());if("update".equals(request.getParameter("action")))value.setAttendanceId(id(request));logic.save(value);flash(session,"勤怠情報を保存しました。","success");}
        }catch(IllegalArgumentException e){flash(session,e.getMessage()==null?"入力内容を確認してください。":e.getMessage(),"error");}
        response.sendRedirect(request.getContextPath()+"/WorkManagement?year="+month.getYear()+"&month="+month.getMonthValue());
    }
    private AttendanceEntity build(HttpServletRequest request,String userId){
        LocalDate date=LocalDate.parse(request.getParameter("workDate"));String type=request.getParameter("workType");if(!WORK_TYPES.contains(type))throw new IllegalArgumentException("勤務区分が不正です。");
        boolean nonWorking="LEAVE".equals(type)||"HOLIDAY".equals(type);LocalTime in=null,out=null;int breakMinutes=0;
        if(!nonWorking){in=LocalTime.parse(request.getParameter("clockIn"));out=LocalTime.parse(request.getParameter("clockOut"));if(!out.isAfter(in))throw new IllegalArgumentException("退勤時刻は出勤時刻より後にしてください。");try{breakMinutes=Integer.parseInt(request.getParameter("breakMinutes"));}catch(Exception e){throw new IllegalArgumentException("休憩時間を正しく入力してください。");}long duration=java.time.temporal.ChronoUnit.MINUTES.between(in,out);if(breakMinutes<0||breakMinutes>=duration)throw new IllegalArgumentException("休憩時間は勤務時間より短くしてください。");}
        String note=trim(request.getParameter("note"));if(note.length()>500)throw new IllegalArgumentException("備考は500文字以内で入力してください。");String reportValue=request.getParameter("reportId");Long reportId=reportValue==null||reportValue.isBlank()?null:Long.valueOf(reportValue);
        return AttendanceEntity.builder().userId(userId).workDate(date).clockIn(in).clockOut(out).breakMinutes(breakMinutes).workType(type).note(note).reportId(reportId).build();
    }
    private YearMonth month(HttpServletRequest request){try{String y=request.getParameter("year"),m=request.getParameter("month");return y==null||m==null?YearMonth.now():YearMonth.of(Integer.parseInt(y),Integer.parseInt(m));}catch(Exception e){return YearMonth.now();}}
    private LocalDate parseDate(String value,LocalDate fallback){try{return value==null?fallback:LocalDate.parse(value);}catch(Exception e){return fallback;}}
    private AttendanceEntity selected(String value,String userId,AttendanceLogic logic){if(value==null)return null;try{return logic.findById(Long.parseLong(value),userId);}catch(Exception e){return null;}}
    private long id(HttpServletRequest request){try{return Long.parseLong(request.getParameter("attendanceId"));}catch(Exception e){throw new IllegalArgumentException("勤怠IDが不正です。");}}
    private String trim(String value){return value==null?"":value.trim();}private void flash(HttpSession session,String message,String type){session.setAttribute("attendanceFlash",message);session.setAttribute("attendanceFlashType",type);}
}
