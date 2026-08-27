package jp.nw.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jp.nw.entity.UserEntity;

@WebServlet(urlPatterns={"/MenuSelect","/BusinessMenu","/CommunicationMenu"})
public class MenuController extends HttpServlet {
    private static final long serialVersionUID=1L;
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        response.setHeader("Cache-Control","no-store");
        String path=request.getServletPath();
        if("/MenuSelect".equals(path)){request.getRequestDispatcher("/WEB-INF/jsp/Menu/menuSelect.jsp").forward(request,response);return;}
        if("/CommunicationMenu".equals(path)){request.getRequestDispatcher("/WEB-INF/jsp/Menu/communicationMenu.jsp").forward(request,response);return;}
        UserEntity user=(UserEntity)request.getSession().getAttribute("loginUser");
        String page="1".equals(user.getPermission())?"/WEB-INF/jsp/Menu/perMenu.jsp":"/WEB-INF/jsp/Menu/genMenu.jsp";
        request.getRequestDispatcher(page).forward(request,response);
    }
}
