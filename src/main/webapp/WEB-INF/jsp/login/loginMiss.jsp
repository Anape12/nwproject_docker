<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8" />
        <title>エラー</title>
    </head>
    <body>
        <p><c:out value="${errorMessage}" /></p>
        <button class="search-btn2" onclick="history.back()">ログイン画面へ</button>
    </body>
</html>
