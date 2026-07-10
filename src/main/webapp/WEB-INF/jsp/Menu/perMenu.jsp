<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="jp.nw.entity.UserEntity" %>
<%
UserEntity loginUser = (UserEntity) session.getAttribute("loginUser");
String userId = loginUser.getUserId();
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style11.css">
<title>メインメニュー（<%=userId %>）</title>
</head>
<body>
<body style="background:#63515f;">
	<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
	<div style="width:200px; margin-top:30px;"></div>
	<div style="text-align:center; background:#d3adf0; width:400px; margin:0 auto;">
		<!-- <div style="text-align:center; width:400px;"> -->
		<!-- <p>ようこそ<%=userId %>さん</p> -->
		<!-- </div> -->
		<div style="margin-top:50px; padding-top:10px;"></div>
		<form action="${pageContext.request.contextPath}/UserSearch" target="_blank"  method="get">
			<p><button class="search-btn4" type="submit">ユーザー情報検索</button></p>
		</form>
		<form action="${pageContext.request.contextPath}/ThreadController" target="_blank"  method="get">
			<p><button class="search-btn4" type="submit">スレッド</button></p>
		</form>
		<form action="${pageContext.request.contextPath}/WorkManagement" target="_blank"  method="get">
			<p><button class="search-btn4" type="submit">勤怠表入力</button></p>
		</form>
		<form action="${pageContext.request.contextPath}/OpenCalender" target="_blank"  method="get">
			<p><button class="search-btn4" type="submit">スケジュール表</button></p>
		</form>
 		<form action="${pageContext.request.contextPath}/UserInsert" target="_blank"  method="get">
			<p><button class="search-btn4" type="submit">新規ユーザー登録</button></p>
		</form>
		<form action="${pageContext.request.contextPath}/DairyWrite" target="_blank"  method="get">
			<p><button class="search-btn4" type="submit">報告書作成</button></p>
		</form>
		<form action="${pageContext.request.contextPath}/DairyWrite" target="_blank"  method="get">
			<p><button class="search-btn4" type="submit">報告書承認</button></p>
		</form>
		<form action="${pageContext.request.contextPath}/UserView" target="_blank"  method="get">
			<p><button class="search-btn4" type="submit">ユーザー一覧参照</button></p>
		</form>
		<form action="${pageContext.request.contextPath}/SelectApp?AppName=NC30001" target="_blank"  method="post">
			<p><button class="search-btn4" type="submit">AX3アプリ</button></p>
		</form>
		<form action="${pageContext.request.contextPath}/SelectApp?AppName=NC40001" target="_blank"  method="post">
			<p><button class="search-btn4" type="submit">AX4アプリ</button></p>
		</form>
	<p><button class="search-btn3" onclick="history.back()">ログアウト</button></p>
		 <jsp:include page="../login/footer.jsp" flush="true"/>
	</div>
</body>
</html>