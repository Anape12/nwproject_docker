<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="jp.nw.model.MyCalendar"%>
<%@ taglib prefix="c"
    uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="utf-8">
  <title>チャット</title>
  <link rel="stylesheet"  href="http://yui.yahooapis.com/3.18.1/build/cssreset/cssreset-min.css">
  <link href="https://fonts.googleapis.com/css?family=M+PLUS+Rounded+1c" rel="stylesheet">
  <link rel="stylesheet"  href="${pageContext.request.contextPath}/css/style.css">
  <link rel="stylesheet"  href="${pageContext.request.contextPath}/css/style33.css">
</head>
<body>

    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
    <div id="container">
        <h1 class="page-title">xxxx(チャット対象者の指名)</h1>

        <span>履歴</span>

    <form action="StartChat"
          method="post"
          class="comment-form">
            <span>入力</span>
            <textarea
                name="commentText"
                maxlength="500"
                required
                placeholder="コメントを入力してください"></textarea>
        </form>
    </div>
</body>