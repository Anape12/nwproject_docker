<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="jp.nw.entity.UserEntity,java.util.List" %>
<%-- <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> --%>
<%
UserEntity loginUser = (UserEntity) session.getAttribute("loginUser");
List<UserEntity> userList = (List<UserEntity>) request.getAttribute("userList");
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
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
<link rel="stylesheet"  href="${pageContext.request.contextPath}/css/style55.css">
<script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
<script>
jQuery(window).on('load', function() {
	jQuery('#loader-bg').hide();
});
</script>
<title>ユーザー検索</title>
</head>
<body>
	<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
	<div style="width:200px"></div>
	<div class="search-form">
		<h1>ユーザー情報検索</h1>
		<form method="post" action="${pageContext.request.contextPath}/UserSearch" name="form1">
			<input type="text" name="userId" placeholder="ユーザーIDを入力してください">
			<button class="search-btn" type="submit">検索</button>
		</form>

		<% if (userList != null && !userList.isEmpty()) { %>
			<table border="1" style="margin-top:20px;">
				<tr class="tr-back">
					<th>No</th>
					<th>ユーザーID</th>
					<th>権限レベル（1:管理者/2:通常）</th>
				</tr>
				<% for(UserEntity userinfo : userList) { %>
				<tr>
					<td><%= userinfo.getId() %></td>
					<td>
						<a href="${pageContext.request.contextPath}/UserSearch?action=edit&userId=<%=userinfo.getUserId()%>">
							<%= userinfo.getUserId() %>
						</a>
					</td>
					<td><%= userinfo.getPermission() %></td>
				</tr>
				<% } %>
			</table>
		<% } %>
	</div>
</body>
<button class="search-btn2" name="userInfo" onclick="window.close();">ウィンドウを閉じる</button>
</html>