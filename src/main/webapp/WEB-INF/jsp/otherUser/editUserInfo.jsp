<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="jp.nw.entity.UserEntity,java.util.List,jp.nw.entity.PermissionMasterEntity" %>
 <%
UserEntity loginUser = (UserEntity) session.getAttribute("loginUser");
List<UserEntity> userList = (List<UserEntity>) request.getAttribute("userList");
String errorMsg = (String) request.getAttribute("errorMsg");
List<PermissionMasterEntity> permissionList = (List<PermissionMasterEntity>) request.getAttribute("permissionLevels");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style55.css">
<script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
<script>
jQuery(window).on('load', function() {
	jQuery('#loader-bg').hide();
});
</script>
<title>ユーザ－情報編集(管理者モード)</title>
</head>
<body>
	<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

	<div class="edit-container">

		<h1>ユーザー情報編集</h1>

		<% if(errorMsg != null){ %>
			<div class="error-message">
				<%=errorMsg %>
			</div>
		<% } %>

		<form method="post"
			action="${pageContext.request.contextPath}/UpdateUserInfo"
			name="form1"
			onsubmit="return checkUserInfo();">

			<% for(UserEntity userinfo : userList){ %>
			<div class="edit-card">
				<div class="form-row">
					<label>現在のユーザーID</label>
					<input type="text"
						name="nowID"
						value="<%=userinfo.getUserId()%>"
						readonly>
				</div>

				<div class="form-row">
					<label>変更後ユーザーID</label>
					<input type="text"
						name="editID"
						value="<%=userinfo.getUserId()%>">
				</div>

				<div class="form-row">
					<label>変更後パスワード</label>
					<input type="password"
						name="editPassword"
						placeholder="変更後パスワード">
				</div>

				<div class="form-row">
					<label>権限レベル</label>
					<select name="editPermission">
						<% for(PermissionMasterEntity permission : permissionList) { %>
							<option value="<%= permission.getPermissionId() %>"
								<%= permission.getPermissionId().equals(userinfo.getPermission()) ? "selected" : "" %>>
								<%= permission.getPermissionName() %>
							</option>
						<% } %>
					</select>

				</div>
			</div>

			<% } %>

			<p class="password-note">
				※ パスワードは半角英数字で入力してください
			</p>

			<div class="button-area">
				<button class="login-btn">
					更新する
				</button>
				<button class="delete-btn"
						type="button">
					削除（工事中）
				</button>

				<button class="close-btn"
						type="button"
						onclick="window.close();">
					閉じる
				</button>
			</div>
		</form>
	</div>
</body>
</html>