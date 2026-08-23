package jp.nw.controller;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import jp.nw.entity.UserEntity;
import jp.nw.model.ApprovalLogic;

@WebServlet("/ReportApproval")
public class ReportApprovalController extends HttpServlet {
    private static final long serialVersionUID=1L;
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        HttpSession session=request.getSession();UserEntity user=(UserEntity)session.getAttribute("loginUser");if(!admin(user)){response.sendError(403,"承認処理は管理者のみ実施できます。");return;}
        ApprovalLogic logic=new ApprovalLogic();String token=(String)session.getAttribute("approvalCsrfToken");if(token==null){token=UUID.randomUUID().toString();session.setAttribute("approvalCsrfToken",token);}
        request.setAttribute("applications",logic.findAll());request.setAttribute("selected",selected(request.getParameter("id"),logic));request.setAttribute("csrfToken",token);
        request.setAttribute("flashMessage",session.getAttribute("approvalFlash"));request.setAttribute("flashType",session.getAttribute("approvalFlashType"));session.removeAttribute("approvalFlash");session.removeAttribute("approvalFlashType");
        request.getRequestDispatcher("/WEB-INF/jsp/report/reportApproval.jsp").forward(request,response);
    }
    @Override protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        request.setCharacterEncoding("UTF-8");HttpSession session=request.getSession(false);UserEntity user=(UserEntity)session.getAttribute("loginUser");if(!admin(user)){response.sendError(403,"承認処理は管理者のみ実施できます。");return;}
        if(!Objects.equals(session.getAttribute("approvalCsrfToken"),request.getParameter("csrfToken"))){response.sendError(403,"不正なリクエストです。");return;}
        try{long id=Long.parseLong(request.getParameter("approvalId"));String decision=request.getParameter("decision"),comment=trim(request.getParameter("comment"));if("REJECTED".equals(decision)&&comment.isBlank())throw new IllegalArgumentException("差戻し理由を入力してください。");if(comment.length()>1000)throw new IllegalArgumentException("コメントは1000文字以内で入力してください。");new ApprovalLogic().review(id,user.getUserId(),decision,comment);flash(session,"APPROVED".equals(decision)?"申請を承認しました。":"申請を差し戻しました。","success");}catch(IllegalArgumentException e){flash(session,e.getMessage(),"error");}
        response.sendRedirect(request.getContextPath()+"/ReportApproval");
    }
    private Object selected(String value,ApprovalLogic logic){if(value==null)return null;try{return logic.findById(Long.parseLong(value));}catch(NumberFormatException e){return null;}}
    private boolean admin(UserEntity user){return user!=null&&"1".equals(user.getPermission());}private String trim(String value){return value==null?"":value.trim();}private void flash(HttpSession s,String m,String t){s.setAttribute("approvalFlash",m);s.setAttribute("approvalFlashType",t);}
}
