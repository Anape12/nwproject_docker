<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ja">
    <head>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width,initial-scale=1" />
        <title>Qiita Guide｜NW Project</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style11.css" />
    </head>
    <body>
        <jsp:include page="/WEB-INF/jsp/common/header.jsp" />
        <main class="workspace-home">
            <section class="menu-section">
                <h1>Qiita Guideを開けません</h1>
                <p>読み上げアプリの接続先が設定されていません。管理者にご確認ください。</p>
                <p><a href="${pageContext.request.contextPath}/BusinessMenu">業務メニューへ戻る</a></p>
            </section>
        </main>
    </body>
</html>
