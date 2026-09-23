<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ja">
    <head>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <title>パスワード変更</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/security.css" />
    </head>
    <body>
        <jsp:include page="/WEB-INF/jsp/common/header.jsp" />

        <main class="security-page narrow">
            <section class="security-card">
                <h1>パスワード変更</h1>
                <p>安全のため、英字と数字を含む8文字以上を設定してください。</p>

                <% if (request.getAttribute("errorMessage") != null) { %>
                <div class="alert error"><%= request.getAttribute("errorMessage") %></div>
                <% } %>

                <form method="post">
                    <input type="hidden" name="csrfToken" value="${csrfToken}" />

                    <label>
                        現在のパスワード
                        <input type="password" name="currentPassword" required autocomplete="current-password" />
                    </label>

                    <label>
                        新しいパスワード
                        <input type="password" name="newPassword" required autocomplete="new-password" />
                    </label>

                    <label>
                        新しいパスワード（確認）
                        <input type="password" name="confirmation" required autocomplete="new-password" />
                    </label>

                    <button class="primary" type="submit">パスワードを変更</button>
                </form>
            </section>
        </main>
    </body>
</html>
