<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
    <head>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style11.css?v=20261010-1" />
        <title>ログインできませんでした | NW Project</title>
    </head>
    <body class="auth-result-page">
        <main class="auth-result-main">
            <div class="auth-result-layout">
                <header class="login-title">
                    <h1>NW Project</h1>
                    <p>Management System</p>
                </header>

                <section class="login-card auth-result-card" role="alert" aria-labelledby="auth-result-heading">
                    <span class="auth-result-icon" aria-hidden="true">!</span>
                    <h2 id="auth-result-heading">ログインできませんでした</h2>
                    <c:choose>
                        <c:when test="${not empty errorMessage}">
                            <p class="auth-result-message"><c:out value="${errorMessage}" /></p>
                        </c:when>
                        <c:otherwise>
                            <p class="auth-result-message">入力内容を確認して、もう一度お試しください。</p>
                        </c:otherwise>
                    </c:choose>
                    <a class="login-btn auth-result-link" href="${pageContext.request.contextPath}/Login"
                        >ログイン画面に戻る</a
                    >
                </section>
            </div>
        </main>
        <footer class="auth-result-footer"><jsp:include page="footer.jsp" /></footer>
    </body>
</html>
