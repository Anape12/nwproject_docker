<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="jp.nw.entity.ChatRoomEntity,java.util.List" %>
<%@ taglib prefix="c"
    uri="http://java.sun.com/jsp/jstl/core" %>
<%
List<ChatRoomEntity> chatRoomList = (List<ChatRoomEntity>) session.getAttribute("chatRooms");
%>

<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="utf-8">
  <title>チャット</title>
  <link rel="stylesheet"  href="http://yui.yahooapis.com/3.18.1/build/cssreset/cssreset-min.css">
  <link href="https://fonts.googleapis.com/css?family=M+PLUS+Rounded+1c" rel="stylesheet">
  <link rel="stylesheet"  href="${pageContext.request.contextPath}/css/style.css">
  <link rel="stylesheet"  href="${pageContext.request.contextPath}/css/style55.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
    <div class="channel-list">

        <c:forEach var="room" items="${chatRooms}">

            <a class="channel-item" href="${pageContext.request.contextPath}/ChatChanelRoom?roomId=${room.roomId}&&displayName=${room.displayName}">

                <div class="channel-icon">
                    <c:choose>
                        <c:when test="${room.roomType == '1'}">
                            👤
                        </c:when>
                        <c:otherwise>
                            👥
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="channel-info">
                    <div class="channel-name">
                        ${room.displayName}
                    </div>

                    <div class="channel-type">
                        <c:choose>
                            <c:when test="${room.roomType == '1'}">
                                個別チャット
                            </c:when>
                            <c:otherwise>
                                グループチャット
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

            </a>

        </c:forEach>
    </div>
</body>