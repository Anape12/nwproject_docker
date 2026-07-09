<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
String errorMsg = (String) request.getAttribute("errorMsg");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>エラー</title>
</head>
<body style="background:#deafd3;">

<h2>エラーが発生しました</h2>

<% if (errorMsg != null) { %>
    <p style="color:red;"><%= errorMsg %></p>
<% } %>

</body>
</html>