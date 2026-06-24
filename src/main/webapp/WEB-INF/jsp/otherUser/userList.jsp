<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="jp.nw.entity.UserEntity,java.util.List" %>
<%-- <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> --%>
<%
UserEntity loginUser = (UserEntity) session.getAttribute("loginUser");
List<UserEntity> userList = (List<UserEntity>) request.getAttribute("userList");
String errorMsg = (String) request.getAttribute("errorMsg");
String userInfo = loginUser.getUserId();
userInfo = userInfo + ":" +loginUser.getPassword();
%>
<!DOCTYPE html>
<script src='http://ajax.aspnetcdn.com/ajax/modernizr/modernizr-2.8.3.js'></script>
<script src='http://code.jquery.com/jquery-1.11.3.min.js'></script>
<script src='http://code.jquery.com/ui/1.11.1/jquery-ui.min.js'></script>
<!-- Ignite UI Required Combined CSS Files -->
<link href='http://cdn-na.infragistics.com/igniteui/2019.2/latest/css/themes/infragistics/infragistics.theme.css' rel='stylesheet' />
<link href='http://cdn-na.infragistics.com/igniteui/2019.2/latest/css/structure/infragistics.css' rel='stylesheet' />
<!-- Ignite UI Required Combined JavaScript Files -->
<script src='http://cdn-na.infragistics.com/igniteui/2019.2/latest/js/infragistics.core.js'></script>
<script src='http://cdn-na.infragistics.com/igniteui/2019.2/latest/js/infragistics.lob.js'></script>

<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet"  href="${pageContext.request.contextPath}/css/style22.css">
<script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
<script>
jQuery(window).on('load', function() {
	jQuery('#loader-bg').hide();
});
</script>
<title>ユーザー一覧(管理者モード)</title>
<%= loginUser.getUserId() %>さん、ログイン中
</head>
<body>
	<div style="width:200px"></div>
	<h1 style="margin-left:450px">ユーザーID：パスワード一覧</h1>
	<div style="width:200px"></div>
	<% if(errorMsg != null) { %>
	<p><%= errorMsg %></p>
	<% } %>
	<form method="post" action="${pageContext.request.contextPath}/UserView" name="form1">
		<table border="7" style="margin-left:450px">
		<tr class="tr-back">
			<th>選択</th>
			<th>No</th>
			<th>ユーザーID</th>
			<th>権限レベル（1:管理者/2:通常）</th>
		</tr>
			<% for(UserEntity userinfo:userList) { %>
			<tr>
			<th><input type="radio" name="radiobutton" value=<%=userinfo.getUserId() %>></th>
			<th><%=userinfo.getId() %></th>
			<th><%=userinfo.getUserId() %></th>
			<th><%=userinfo.getPermission() %></th>
		</tr>
		<%} %>
		</table>
		<button class="search-btn3" style="margin-left:570px; margin-top:30px" type="submit" name="action" value="change" onclick="return checkForm();">
			変更画面へ
		</button>
		<br/>
		<button class="search-btn3" style="margin-left:570px; margin-top:30px" type="submit" name="action" value="reset" onclick="return checkForm();">
			パスワードを初期化
		</button>
	</form>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/UserListCheck.js"></script>
</body>
<button class="search-btn2" style="margin-left:570px; margin-top:30px" name="userInfo" onclick="window.close();">ウィンドウを閉じる</button>
</html>