<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="jp.nw.model.User" %>
<%
User loginUser = (User) session.getAttribute("loginUser");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
<title>登録情報確認</title>
</head>
<body style="background:#9ad468;">
	<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
	<form action="${pageContext.request.contextPath}/UserInsertResult" method="get">
		<div style="text-align:center;">
		<p style="text-align:center">以下の情報で登録します</p>
		<table border="1" align="center">
		  <tr>
		      <th>ユーザーID（a始まりの5桁）</th>
		      <th>パスワード</th>
		      <th>生年月日（例：1990-01-01(yyyy-mm-dd)）</th>
		      <th>権限レベル（1:管理者/2:通常/）</th>
	  	</tr>
	  	<tr>
  		    <th><input type="text" name="userId" value="${loginUser.getName()}" readonly></th>
  		    <th><input type="text" name="userPass" value="${loginUser.getPass()}" readonly></th>
  		    <th><input type="text" name="userBirth" value="${loginUser.getBirth()}" readonly></th>
  		    <th><input type="text" name="userPermis" value="${loginUser.getPermission()}" readonly></th>	  	
	  	</tr>
	  </table>
			<p><button  class="search-btn3" type="submit">確定</button></p>
		</div>
	</form>
	<div style="text-align:center;">
		<p><button class="search-btn2" onclick="window.close();">ウィンドウを閉じる</button></p>
	</div>
</body>
</html>