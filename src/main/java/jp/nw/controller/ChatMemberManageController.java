package jp.nw.controller;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import jp.nw.entity.UserEntity;
import jp.nw.parts.DBBase;

@WebServlet("/ChatMemberManage")
public class ChatMemberManageController extends HttpServlet {
    private static final long serialVersionUID=1L;
    protected void doGet(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{UserEntity u=user(q);String room=q.getParameter("roomId");if(!canManage(u,room)){s.sendError(403);return;}HttpSession session=q.getSession();if(session.getAttribute("chatMemberCsrf")==null)session.setAttribute("chatMemberCsrf",UUID.randomUUID().toString());q.setAttribute("roomId",room);q.setAttribute("availableUsers",available(room));q.setAttribute("members",members(room));q.setAttribute("flash",session.getAttribute("chatMemberFlash"));session.removeAttribute("chatMemberFlash");q.getRequestDispatcher("/WEB-INF/jsp/chat/ChatMemberManage.jsp").forward(q,s);}
    protected void doPost(HttpServletRequest q,HttpServletResponse s)throws IOException{q.setCharacterEncoding("UTF-8");UserEntity u=user(q);HttpSession session=q.getSession(false);String room=q.getParameter("roomId");if(!canManage(u,room)){s.sendError(403);return;}if(!Objects.equals(session.getAttribute("chatMemberCsrf"),q.getParameter("csrfToken"))){s.sendError(403);return;}DBBase db=new DBBase();try(Connection c=db.getConnection();PreparedStatement p=c.prepareStatement("INSERT IGNORE INTO chat_room_member(room_id,user_id) SELECT ?,user_id FROM users_info WHERE user_id=? AND delete_flg='0'")){p.setString(1,room);p.setString(2,q.getParameter("userId"));session.setAttribute("chatMemberFlash",p.executeUpdate()==1?"メンバーを招待しました。":"既に参加済みか、利用できないユーザーです。");}catch(Exception e){session.setAttribute("chatMemberFlash","招待に失敗しました。");}s.sendRedirect(q.getContextPath()+"/ChatMemberManage?roomId="+java.net.URLEncoder.encode(room,java.nio.charset.StandardCharsets.UTF_8));}
    private boolean canManage(UserEntity u,String room){if(u==null||room==null)return false;DBBase db=new DBBase();try(Connection c=db.getConnection();PreparedStatement p=c.prepareStatement("SELECT 1 FROM chat_room WHERE room_id=? AND room_type='2' AND delete_flg='0' AND (created_by_id=? OR ?='1')")){p.setString(1,room);p.setString(2,u.getUserId());p.setString(3,u.getPermission());try(ResultSet r=p.executeQuery()){return r.next();}}catch(Exception e){return false;}}
    private List<UserEntity> available(String room){return users("SELECT user_id,first_name,last_name,account_type FROM users_info WHERE delete_flg='0' AND user_id NOT IN(SELECT user_id FROM chat_room_member WHERE room_id=?) ORDER BY account_type DESC,last_name,first_name",room);}
    private List<UserEntity> members(String room){return users("SELECT u.user_id,u.first_name,u.last_name,u.account_type FROM users_info u JOIN chat_room_member m ON m.user_id=u.user_id WHERE m.room_id=? ORDER BY u.account_type DESC,u.last_name,u.first_name",room);}
    private List<UserEntity> users(String sql,String room){List<UserEntity> list=new ArrayList<>();DBBase db=new DBBase();try(Connection c=db.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,room);try(ResultSet r=p.executeQuery()){while(r.next())list.add(UserEntity.builder().userId(r.getString(1)).firstName(r.getString(2)).lastName(r.getString(3)).accountType(r.getString(4)).build());}}catch(Exception e){throw new RuntimeException(e);}return list;}
    private UserEntity user(HttpServletRequest q){HttpSession s=q.getSession(false);return s==null?null:(UserEntity)s.getAttribute("loginUser");}
}
