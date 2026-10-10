<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
    <head>
        <meta charset="UTF-8" />
        <title>エラー</title>
    </head>
    <body style="background:#deafd3;">
        <h2>エラーが発生しました</h2>

        <c:if test="${not empty errorMsg}"
            ><p style="color:red;"><c:out value="${errorMsg}" /></p
        ></c:if>
    </body>
</html>
