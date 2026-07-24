<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c"
    uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="utf-8">
  <title>新規スレッド</title>
  <link rel="stylesheet"  href="http://yui.yahooapis.com/3.18.1/build/cssreset/cssreset-min.css">
  <link href="https://fonts.googleapis.com/css?family=M+PLUS+Rounded+1c" rel="stylesheet">
  <link rel="stylesheet"  href="${pageContext.request.contextPath}/css/style.css">
  <link rel="stylesheet"  href="${pageContext.request.contextPath}/css/style33.css">
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
    <div id="container">
    <div class="thread-card">
        <h1 class="page-title">新規スレッド</h1>

        <form action="${pageContext.request.contextPath}/ThreadCreate" method="post">

            <div class="form-group">
                <label for="title">タイトル</label>
                <input
                    type="text"
                    id="title"
                    name="title"
                    placeholder="スレッドタイトルを入力してください"
                    required>
            </div>

            <div class="form-group">
                <label for="content">詳細</label>
                <textarea
                    id="content"
                    name="content"
                    rows="8"
                    placeholder="質問内容や相談内容を入力してください"
                    required></textarea>
            </div>

            <div class="button-area">
                <button type="submit" class="primary-btn">
                    スレッドを作成
                </button>
            </div>

        </form>
    </div>
</div>
</body>
</html>
