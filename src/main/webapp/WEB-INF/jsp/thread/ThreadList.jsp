<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="jp.nw.model.MyCalendar"%>
<%@ taglib prefix="c"
    uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="utf-8">
  <title>スレッド一覧</title>
  <link rel="stylesheet"  href="http://yui.yahooapis.com/3.18.1/build/cssreset/cssreset-min.css">
  <link href="https://fonts.googleapis.com/css?family=M+PLUS+Rounded+1c" rel="stylesheet">
  <link rel="stylesheet"  href="${pageContext.request.contextPath}/css/style.css">
  <link rel="stylesheet"  href="${pageContext.request.contextPath}/css/style33.css">
</head>
<body>

    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
    <div id="container">
        <h1 class="page-title">スレッド一覧</h1>
        <a class="new-thread-link" href="${pageContext.request.contextPath}/ThreadCreate">
            ＋ 新規スレッド
        </a>
        <c:forEach var="thread" items="${threadList}">
            <div class="thread-card">
                <div class="thread-header">
                    <h2>${thread.title}</h2>
                    <span class="thread-status ${thread.status}">${thread.closed ? '完了' : '進行中'}</span>
                </div>

                <div class="thread-content">
                    ${thread.content}
                </div>

                <div class="thread-footer">
                    <span class="thread-author">投稿者：<c:out value="${thread.authorId}"/></span>
                    <a href="${pageContext.request.contextPath}/ThreadDetailController?id=${thread.threadId}">
                        詳細を見る →
                    </a>
                </div>
            </div>
        </c:forEach>
    </div>
</body>
