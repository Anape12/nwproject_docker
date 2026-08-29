<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="utf-8">
  <title>個別チャット作成</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style55.css">
</head>
<body>
  <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  <main class="chat-create-container">
    <div class="chat-create-heading">
      <div>
        <h1>個別チャットを開始</h1>
        <p>チャットしたいユーザーを選択してください。</p>
      </div>
      <a class="chat-secondary-btn" href="${pageContext.request.contextPath}/ChatChanelList">戻る</a>
    </div>

    <c:if test="${not empty errorMessage}">
      <p class="chat-error"><c:out value="${errorMessage}"/></p>
    </c:if>

    <div class="chat-user-list">
      <c:forEach var="user" items="${chatUsers}">
        <form class="chat-user-item" method="post" action="${pageContext.request.contextPath}/ChatRoomCreate">
          <div class="channel-icon">${user.accountType == 'AI' ? 'AI' : '👤'}</div>
          <div class="chat-user-info">
            <strong><c:out value="${user.lastName}"/> <c:out value="${user.firstName}"/> <c:if test="${user.accountType == 'AI'}"><span class="ai-user-badge">AI</span></c:if></strong>
            <span><c:out value="${user.userId}"/></span>
          </div>
          <input type="hidden" name="targetUserId" value="<c:out value='${user.userId}'/>">
          <button class="chat-start-btn" type="submit">チャットを開始</button>
        </form>
      </c:forEach>
      <c:if test="${empty chatUsers}">
        <p class="chat-empty">チャットを開始できるユーザーがいません。</p>
      </c:if>
    </div>
  </main>
</body>
</html>
