<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="jp.nw.entity.UserEntity" %>
<% UserEntity loginUser=(UserEntity)session.getAttribute("loginUser"); %>
<!DOCTYPE html><html lang="ja"><head><meta charset="UTF-8"><title>メインメニュー（<%=loginUser.getUserId()%>）</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style11.css"><link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.6.0/css/all.min.css"></head><body>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/><div class="menu-container"><h1>メインメニュー</h1><div class="menu-grid">
<form action="${pageContext.request.contextPath}/DairyWrite" method="get" target="_blank"><button class="menu-btn" type="submit"><i class="fa-solid fa-file-pen"></i><span>報告書作成</span></button></form>
<form action="${pageContext.request.contextPath}/OpenCalender" method="get" target="_blank"><button class="menu-btn" type="submit"><i class="fa-solid fa-calendar-days"></i><span>スケジュール表</span></button></form>
<form action="${pageContext.request.contextPath}/ChatChanelList" method="get" target="_blank"><button class="menu-btn" type="submit"><i class="fa-solid fa-comments"></i><span>チャット</span></button></form>
<form action="${pageContext.request.contextPath}/ThreadController" method="get" target="_blank"><button class="menu-btn" type="submit"><i class="fa-solid fa-users"></i><span>スレッド</span></button></form>
</div><form action="${pageContext.request.contextPath}/Logout" method="get"><button class="logout-btn" type="submit"><i class="fa-solid fa-right-from-bracket"></i> ログアウト</button></form></div><jsp:include page="/WEB-INF/jsp/login/footer.jsp"/></body></html>
