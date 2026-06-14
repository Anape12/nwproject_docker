<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="jp.nw.entity.UserEntity" %>
<%@page import="java.util.*, java.io.*" %>
<%
UserEntity loginUser = (UserEntity) session.getAttribute("loginUser");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style11.css">
<title><%= loginUser.getUserId() %>さん　ログイン中</title>
</head>
<body style="background:#9ad468;">
	<form action="${pageContext.request.contextPath}/UserInsert" method="post">
		<div style="text-align:center;">
		<table border="1" align="center">
		  <tr>
		      <th>ユーザーID（a始まりの5桁）</th>
		      <th>パスワード</th>
		      <th>生年月日（例：1990-01-01(yyyy-mm-dd)）</th>
		      <th>権限レベル（1:管理者/2:通常/）</th>
	  	</tr>
	  	<tr>
	  		<th><input class="title-txt" type="text" name="userid" placeholder="ユーザーID"></th>
	  		<th><input class="title-txt" type="text" name="userpass" placeholder="パスワード"></th>
	  		<th>
	  			<select name="birthYear">
					<%
					Calendar c = Calendar.getInstance();
					for( int i=1930; i<=c.get(Calendar.YEAR); i++ ){
					String s = Integer.valueOf(i).toString();
					%>
					<option value="<%= s %>"><%= s %></option>
					<% } %>
				</select>-
				<select name="birthMonth">
					<%
					for( int j=1; j<=12; j++ ){
					String ss = Integer.valueOf(j).toString();
					%>
					<option value="<%= ss %>"><%= ss %></option>
					<% } %>
				</select>-
				<select name="birthDate">
					<%
					for( int k=1; k<=31; k++ ){
					String sd = Integer.valueOf(k).toString();
					%>
					<option value="<%= sd %>"><%= sd %></option>
					<% } %>
				</select>
	  		</th>
	  		<th><input class="title-txt" type="text" name="userpermis" placeholder="権限レベル"></th>
	  	</tr>
	  </table>
			<p><button  class="search-btn3" type="submit">確認</button></p>
		</div>
	</form>
	<div style="text-align:center;">
		<p><button class="search-btn2" onclick="window.close();">ウィンドウを閉じる</button></p>
	</div>
</body>
</html>