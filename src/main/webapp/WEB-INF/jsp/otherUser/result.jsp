<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ja">
    <head>
        <meta charset="utf-8" />
    </head>
    <body style="background:#deafd3;">
        <div>登録が完了しました</div>
        <form action="${pageContext.request.contextPath}/UserView" target="_blank" method="get">
            <p><button class="search-btn3" type="submit">ユーザー一覧へ戻る</button></p>
        </form>
    </body>
</html>
