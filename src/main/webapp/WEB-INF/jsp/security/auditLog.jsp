<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="ja">
    <head>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <title>監査ログ</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/security.css?v=20260923-1" />
    </head>
    <body>
        <jsp:include page="/WEB-INF/jsp/common/header.jsp" />

        <main class="security-page">
            <div class="page-head">
                <div>
                    <p>AUDIT TRAIL</p>
                    <h1>監査ログ検索</h1>
                </div>
                <a href="${pageContext.request.contextPath}/UserSecurityAdmin">アカウント管理</a>
            </div>

            <form class="audit-filter" method="get">
                <label>
                    ユーザーID
                    <input name="userId" value="${fn:escapeXml(param.userId)}" />
                </label>

                <label>
                    分類
                    <select name="category">
                        <option value="">すべて</option>
                        <c:forEach var="category" items="AUTH,USER,SECURITY,ATTENDANCE,APPROVAL">
                            <c:choose>
                                <c:when test="${param.category == category}">
                                    <option value="${category}" selected><c:out value="${category}" /></option>
                                </c:when>
                                <c:otherwise>
                                    <option value="${category}"><c:out value="${category}" /></option>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </select>
                </label>

                <label>
                    操作
                    <input name="action" value="${fn:escapeXml(param.action)}" placeholder="LOGIN, APPROVED..." />
                </label>

                <label>
                    結果
                    <select name="success">
                        <option value="">すべて</option>
                        <c:choose>
                            <c:when test="${param.success == 'true'}">
                                <option value="true" selected>成功</option>
                            </c:when>
                            <c:otherwise>
                                <option value="true">成功</option>
                            </c:otherwise>
                        </c:choose>
                        <c:choose>
                            <c:when test="${param.success == 'false'}">
                                <option value="false" selected>失敗</option>
                            </c:when>
                            <c:otherwise>
                                <option value="false">失敗</option>
                            </c:otherwise>
                        </c:choose>
                    </select>
                </label>

                <label>
                    開始日
                    <input type="date" name="from" value="${param.from}" />
                </label>

                <label>
                    終了日
                    <input type="date" name="to" value="${param.to}" />
                </label>

                <button class="primary" type="submit">検索</button>
            </form>

            <div class="audit-table-wrap">
                <table class="audit-table">
                    <thead>
                        <tr>
                            <th>日時</th>
                            <th>分類／操作</th>
                            <th>実行者</th>
                            <th>対象</th>
                            <th>結果</th>
                            <th>IP</th>
                            <th>詳細</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="log" items="${logs}">
                            <tr>
                                <td><c:out value="${log.createdAt}" /></td>
                                <td>
                                    <strong><c:out value="${log.eventCategory}" /></strong><br />
                                    <c:out value="${log.eventAction}" />
                                </td>
                                <td><c:out value="${empty log.actorUserId ? '-' : log.actorUserId}" /></td>
                                <td>
                                    <c:out value="${log.targetType}" /><br />
                                    <c:out value="${log.targetId}" />
                                </td>
                                <td>
                                    <span class="status ${log.success ? 'active' : 'disabled'}">
                                        ${log.success ? '成功' : '失敗'}
                                    </span>
                                </td>
                                <td><c:out value="${log.ipAddress}" /></td>
                                <td><c:out value="${log.detail}" /></td>
                            </tr>
                        </c:forEach>

                        <c:if test="${empty logs}">
                            <tr>
                                <td colspan="7" class="empty">該当する監査ログはありません。</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </main>
    </body>
</html>
