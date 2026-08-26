package jp.nw.controller;

import java.io.IOException;
import javax.servlet.*;import javax.servlet.annotation.WebServlet;import javax.servlet.http.*;
import jp.nw.entity.UserEntity;import jp.nw.model.PortalLogic;

@WebServlet("/Portal")
public class PortalController extends HttpServlet {
 protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{UserEntity u=(UserEntity)req.getSession().getAttribute("loginUser");PortalLogic l=new PortalLogic();req.setAttribute("dashboard",l.dashboard(u.getUserId(),"1".equals(u.getPermission())));req.setAttribute("query",req.getParameter("q"));req.setAttribute("searchResults",l.search(u.getUserId(),req.getParameter("q")));req.getRequestDispatcher("/WEB-INF/jsp/portal/dashboard.jsp").forward(req,res);}
 protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException{UserEntity u=(UserEntity)req.getSession().getAttribute("loginUser");try{if(req.getParameter("approvalId")!=null)new jp.nw.model.ApprovalLogic().withdraw(Long.parseLong(req.getParameter("approvalId")),u.getUserId());else new PortalLogic().markRead(u.getUserId(),Long.parseLong(req.getParameter("notificationId")));}catch(Exception e){req.getSession().setAttribute("portalError",e.getMessage());}res.sendRedirect(req.getContextPath()+"/Portal");}
}
