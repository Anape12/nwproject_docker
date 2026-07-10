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
	<p style="text-align:center">登録が完了しました</p>
	<div style="text-align:center;">
		<p><button class="search-btn2" onclick="window.close();">ウィンドウを閉じる</button></p>
	</div>
</body>
</html>