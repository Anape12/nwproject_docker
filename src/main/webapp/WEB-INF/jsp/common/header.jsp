<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="jp.nw.entity.UserEntity" %>

<%
UserEntity loginUser =
    (UserEntity)session.getAttribute("loginUser");
%>

<div class="header">
    <div class="logo">
        NW Project
    </div>
    <div class="login-user">
        <% if(loginUser != null){ %>
            <%=loginUser.getUserId()%> さん ログイン中
        <% } %>
    </div>

</div>