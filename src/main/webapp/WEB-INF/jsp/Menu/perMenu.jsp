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
	<title>メインメニュー（<%=userId%>）</title>
	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style11.css">
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.6.0/css/all.min.css">
</head>
<body>
    <!-- 共通ヘッダー -->
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
    <div class="menu-container">
        <h1>メインメニュー</h1>
        <div class="menu-grid">
            <form action="${pageContext.request.contextPath}/ChatChanelList"
                  method="get"
                  target="_blank">
                <button class="menu-btn" type="submit">
                    <i class="fa-solid fa-users"></i>
                    <span>チャットを開始する</span>
                </button>
            </form>

            <form action="${pageContext.request.contextPath}/UserSearch"
                  method="get"
                  target="_blank">
                <button class="menu-btn" type="submit">
                    <i class="fa-solid fa-users"></i>
                    <span>ユーザー情報検索</span>
                </button>
            </form>

            <form action="${pageContext.request.contextPath}/ThreadController"
                  method="get"
                  target="_blank">
                <button class="menu-btn" type="submit">
                    <i class="fa-solid fa-comments"></i>
                    <span>スレッド</span>
                </button>
            </form>

            <form action="${pageContext.request.contextPath}/WorkManagement"
                  method="get"
                  target="_blank">
                <button class="menu-btn" type="submit">
                    <i class="fa-solid fa-business-time"></i>
                    <span>勤怠表入力</span>
                </button>
            </form>

            <form action="${pageContext.request.contextPath}/OpenCalender"
                  method="get"
                  target="_blank">
                <button class="menu-btn" type="submit">
                    <i class="fa-solid fa-calendar-days"></i>
                    <span>スケジュール表</span>
                </button>
            </form>

            <form action="${pageContext.request.contextPath}/UserInsert"
                  method="get"
                  target="_blank">
                <button class="menu-btn" type="submit">
                    <i class="fa-solid fa-user-plus"></i>
                    <span>新規ユーザー登録</span>
                </button>
            </form>

            <form action="${pageContext.request.contextPath}/DairyWrite"
                  method="get"
                  target="_blank">
                <button class="menu-btn" type="submit">
                    <i class="fa-solid fa-file-pen"></i>
                    <span>報告書作成</span>
                </button>
            </form>

            <form action="${pageContext.request.contextPath}/ReportApproval"
                  method="get"
                  target="_blank">
                <button class="menu-btn" type="submit">
                    <i class="fa-solid fa-circle-check"></i>
                    <span>報告書承認</span>
                </button>
            </form>

            <!-- <form action="${pageContext.request.contextPath}/UserView"
                  method="get"
                  target="_blank">
                <button class="menu-btn" type="submit">
                    <i class="fa-solid fa-list"></i>
                    <span>ユーザー一覧</span>
                </button>
            </form> -->

            <form action="${pageContext.request.contextPath}/SelectApp?AppName=NC30001"
                  method="post"
                  target="_blank">
                <button class="menu-btn" type="submit">
                    <i class="fa-solid fa-cubes"></i>
                    <span>AX3アプリ</span>
                </button>
            </form>

            <form action="${pageContext.request.contextPath}/SelectApp?AppName=NC40001"
                  method="post"
                  target="_blank">
                <button class="menu-btn" type="submit">
                    <i class="fa-solid fa-layer-group"></i>
                    <span>AX4アプリ</span>
                </button>
            </form>

        </div>

        <form action="${pageContext.request.contextPath}/Logout" method="get">
            <button class="logout-btn" type="submit">
                <i class="fa-solid fa-right-from-bracket"></i>
                ログアウト
            </button>
        </form>

    </div>

    <jsp:include page="/WEB-INF/jsp/login/footer.jsp"/>

</body>

</html>
