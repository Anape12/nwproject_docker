<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="jp.nw.entity.UserEntity,jp.nw.entity.ChatMessageEntity,java.util.List,jp.nw.util.DateFormatUtil" %>
<%@ taglib prefix="c"
    uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ja">
<script>
    const loginUserId = "${loginUser.userId}";
    const contextPath = "${pageContext.request.contextPath}";
</script>
<head>
  <meta charset="utf-8">
  <title>チャット</title>
  <link rel="stylesheet"  href="http://yui.yahooapis.com/3.18.1/build/cssreset/cssreset-min.css">
  <link href="https://fonts.googleapis.com/css?family=M+PLUS+Rounded+1c" rel="stylesheet">
  <link rel="stylesheet"  href="${pageContext.request.contextPath}/css/style.css">
  <link rel="stylesheet"  href="${pageContext.request.contextPath}/css/chatLayout.css">
</head>
<body class="chat-page">

    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
    <div id="container" class="chat-container">
        <h1 class="page-title"><c:out value="${RoomName}"/></h1><c:if test="${canManageMembers}"><a class="member-manage-link" href="${pageContext.request.contextPath}/ChatMemberManage?roomId=${RoomId}">メンバーを招待</a></c:if>

        <div id="chat-area" class="chat-area">

            <c:forEach var="message" items="${chatDetail}">

                <c:choose>

                    <c:when test="${message.postedById == loginUser.userId}">
                        <div class="chat-message my-message">
                            <div class="message-body">
                                <c:out value="${message.message}"/>
                            </div>
                            <span class="message-time">
                                ${DateFormatUtil.format(message.createdAt)}
                            </span>                        
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="chat-message other-message">
                            <div class="message-user">
                                <c:out value="${message.postedByName}"/> <c:if test="${message.postedByAccountType == 'AI'}"><span class="ai-user-badge">AI</span></c:if>
                            </div>

                            <div class="message-body">
                                <c:out value="${message.message}"/>
                            </div>

                            <div class="message-time">
                                ${message.createdAt}
                            </div>
                        </div>
                    </c:otherwise>

                </c:choose>

            </c:forEach>

        </div>

        <form id="chat-form" class="comment-form">
            <input type="hidden" id="roomId" name="roomId" value="${RoomId}"/>
            <textarea id="commentText" name="commentText" maxlength="500" rows="1" required
                placeholder="コメントを入力（Shift + Enterで改行）"></textarea>
            <button type="submit">送信</button>
        </form>
        <div class="attachments"><c:forEach var="f" items="${attachments}"><a href="${pageContext.request.contextPath}/Attachment?id=${f.attachment_id}"><c:out value="${f.original_name}"/></a></c:forEach><form method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/Attachment"><input type="hidden" name="ownerType" value="CHAT"><input type="hidden" name="ownerId" value="${RoomId}"><input type="file" name="file" required><button>ファイル送信</button></form></div>

        <script type="text/javascript" src="${pageContext.request.contextPath}/js/Chat.js"></script>
    </div>
</body>
