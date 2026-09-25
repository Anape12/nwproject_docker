<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="jp.nw.model.User" %>
<% User loginUser = (User) session.getAttribute("loginUser"); %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
        <title>日記ページ</title>
    </head>
    <body style="background:#9ad468;">
        <form action="/example/DairyWrite" method="post">
            <div style="text-align:center;">
                <p><input class="title-txt" type="text" name="title" placeholder="タイトル" /></p>
                <p><input class="text-txt" type="text" name="text1" placeholder="本文を記入" /></p>
                <p><button class="search-btn3" type="submit">投稿</button></p>
            </div>
        </form>
        <div style="text-align:center;">
            <p><button class="search-btn2" onclick="history.back()">戻る</button></p>
        </div>
    </body>
</html>
