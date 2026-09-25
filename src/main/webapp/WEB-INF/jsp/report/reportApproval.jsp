<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
    <head>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width,initial-scale=1" />
        <title>承認管理</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=20260925-1" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/report.css?v=20260925-1" />
    </head>
    <body>
        <jsp:include page="/WEB-INF/jsp/common/header.jsp" />
        <main class="report-page">
            <div class="report-heading">
                <div>
                    <p class="eyebrow">APPROVAL CENTER</p>
                    <h1>承認管理</h1>
                    <p>各種申請を共通画面で確認・承認します。</p>
                </div>
            </div>
            <c:if test="${not empty flashMessage}"
                ><div class="notice ${flashType}"><c:out value="${flashMessage}" /></div
            ></c:if>
            <c:if test="${sessionScope.loginUser.permission=='1'}"
                ><div class="batch-approval">
                    <form method="post">
                        <input type="hidden" name="csrfToken" value="${csrfToken}" />
                        <input type="hidden" name="action" value="route" />
                        <label class="batch-field">
                            申請の種類
                            <select name="applicationType">
                                <option value="REPORT">報告書</option>
                                <option value="ATTENDANCE">勤怠</option>
                            </select>
                        </label>
                        <label class="batch-field batch-field-short">
                            承認段階
                            <input type="number" name="requiredSteps" min="1" max="5" value="1" />
                        </label>
                        <button class="submit" type="submit">承認段階を設定</button>
                    </form>
                    <form method="post">
                        <input type="hidden" name="csrfToken" value="${csrfToken}" />
                        <input type="hidden" name="action" value="delegate" />
                        <label class="batch-field">
                            代理承認者ID
                            <input name="delegateUserId" required placeholder="ユーザーID" />
                        </label>
                        <label class="batch-field">
                            開始日
                            <input type="date" name="validFrom" required />
                        </label>
                        <label class="batch-field">
                            終了日
                            <input type="date" name="validTo" required />
                        </label>
                        <button class="submit" type="submit">代理承認を設定</button>
                    </form>
                </div></c:if
            >
            <form class="batch-approval" method="post">
                <input type="hidden" name="csrfToken" value="${csrfToken}" />
                <strong class="batch-heading">承認待ちを一括処理</strong>
                <div>
                    <c:forEach var="a" items="${applications}"
                        ><c:if test="${a.status=='SUBMITTED'}"
                            ><label
                                ><input type="checkbox" name="approvalIds" value="${a.approvalId}" />
                                <c:out value="${a.title}" /></label></c:if
                    ></c:forEach>
                </div>
                <label class="batch-field batch-comment">
                    処理コメント
                    <input name="comment" maxlength="1000" placeholder="必要に応じて入力" /> </label
                ><button class="submit" name="decision" value="APPROVED">一括承認</button
                ><button class="danger" name="decision" value="REJECTED">一括差戻し</button>
            </form>
            <div class="report-layout">
                <section class="report-list">
                    <div class="list-title"><h2>申請一覧</h2></div>
                    <c:forEach var="a" items="${applications}"
                        ><a class="report-list-item" href="?id=${a.approvalId}"
                            ><div>
                                <strong
                                    ><span class="status ${a.status}">${a.typeLabel}</span>
                                    <c:out value="${a.title}" /></strong
                                ><span><c:out value="${a.applicantName}" /> ・ ${a.targetDate}</span>
                            </div>
                            <span class="status ${a.status}">${a.statusLabel}</span></a
                        ></c:forEach
                    ><c:if test="${empty applications}"><p class="empty">申請はありません。</p></c:if>
                </section>
                <section class="report-editor">
                    <c:choose
                        ><c:when test="${empty selected}"
                            ><div class="select-guide">左の一覧から申請を選択してください。</div></c:when
                        ><c:otherwise
                            ><div class="report-readonly">
                                <div class="meta">
                                    <span
                                        >${selected.typeLabel} ・ <c:out value="${selected.applicantName}" /> ・
                                        ${selected.targetDate}</span
                                    ><span class="status ${selected.status}">${selected.statusLabel}</span>
                                </div>
                                <h2><c:out value="${selected.title}" /></h2>
                                <div class="report-body"><c:out value="${selected.detail}" /></div>
                                <div class="approval-history">
                                    <h3>承認履歴</h3>
                                    <c:forEach var="h" items="${approvalHistory}"
                                        ><p>
                                            <b>${h.action}</b> ・ <c:out value="${h.acted_by}" /> ・ ${h.acted_at}<br /><c:out
                                                value="${h.comment}"
                                            /></p
                                    ></c:forEach>
                                </div>
                                <c:choose
                                    ><c:when test="${selected.status=='SUBMITTED'}"
                                        ><form class="approval-form" method="post">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}" /><input
                                                type="hidden"
                                                name="approvalId"
                                                value="${selected.approvalId}"
                                            /><label
                                                >承認者コメント<textarea
                                                    name="comment"
                                                    maxlength="1000"
                                                    rows="5"
                                                ></textarea>
                                            </label>
                                            <div class="inline-actions">
                                                <button class="submit" name="decision" value="APPROVED">承認する</button
                                                ><button class="danger" name="decision" value="REJECTED">
                                                    差し戻す
                                                </button>
                                            </div>
                                        </form></c:when
                                    ><c:otherwise
                                        ><div class="review-comment">
                                            <strong>処理結果</strong>
                                            <p><c:out value="${selected.reviewComment}" /></p>
                                            <small
                                                ><c:out value="${selected.reviewerName}" /> ・
                                                ${selected.reviewedAtLabel}</small
                                            >
                                        </div></c:otherwise
                                    ></c:choose
                                >
                            </div></c:otherwise
                        ></c:choose
                    >
                </section>
            </div>
        </main>
    </body>
</html>
