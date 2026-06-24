<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="jp.nw.entity.UserEntity,java.util.List" %>
 <%
UserEntity loginUser = (UserEntity) session.getAttribute("loginUser");
List<UserEntity> userList = (List<UserEntity>) request.getAttribute("userList");
String errorMsg = (String) request.getAttribute("errorMsg");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style11.css">
<script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
<script>
jQuery(window).on('load', function() {
	jQuery('#loader-bg').hide();
});
</script>
<title>ユーザ－情報編集(管理者モード)</title>
<%= loginUser.getUserId() %>さん、ログイン中
</head>
<body>
	<div style="width:200px"></div>
	<h1 style="margin-left:550px">ユーザー情報変更</h1>
	<div style="width:200px"></div>
	<% if(errorMsg != null) { %>
	<p><%= errorMsg %></p>
	<% } %>
	<form method="post" action="${pageContext.request.contextPath}/EditUserView" name="form1" onsubmit="event.preventDefault(); checkUserInfo();">
		<table border="1" style="margin-left:350px">
	  <tr>
	      <th>変更前ユーザーID</th>
	      <th>変更後ユーザーID</th>
	      <th>変更後権限レベル（1:管理者/2:通常）</th>
	  </tr>
		<% for(UserEntity userinfo:userList) { %>
		<tr>
		  <th><input type="text" name="nowID" value=<%=userinfo.getUserId() %> readonly style="background-color:#e9e9e9"></th>
		  <th><input type="text" name="editID" value=<%=userinfo.getUserId() %>></th>
		  <th><input type="text" name="editPermission" value=<%=userinfo.getPermission() %>></th>
		</tr>

		<% } %>
		</table>
		<input class="search-btn3" style="margin-left:570px; margin-top:30px" type="submit" name="change" value="変更確定">
	</form>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/UserListCheck.js"></script>
	<input class="search-btn3" style="margin-left:570px; margin-top:30px" type="submit" name="change" value="削除確定(工事中)">
	<br/>
	<button class="search-btn2" style="margin-left:570px; margin-top:30px" name="userInfo" onclick="window.close();">ウィンドウを閉じる</button>
</body>
</html>