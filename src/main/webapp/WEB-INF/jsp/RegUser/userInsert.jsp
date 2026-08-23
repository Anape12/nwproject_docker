<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>新規ユーザー登録</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user-registration.css">
</head>
<body>
  <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  <main class="registration-page">
    <div class="page-heading">
      <div><p class="eyebrow">USER MANAGEMENT</p><h1>新規ユーザー登録</h1><p>業務システムを利用するユーザーの基本情報とログイン情報を登録します。</p></div>
      <button type="button" class="secondary-button" onclick="window.close()">閉じる</button>
    </div>

    <c:if test="${not empty successMessage}"><div class="notice success"><span>✓</span><c:out value="${successMessage}"/></div></c:if>
    <c:if test="${not empty errorMessage}"><div class="notice error"><span>!</span><c:out value="${errorMessage}"/></div></c:if>

    <form class="registration-card" method="post" action="${pageContext.request.contextPath}/UserInsert">
      <input type="hidden" name="csrfToken" value="${csrfToken}">
      <section><div class="section-heading"><span>1</span><div><h2>基本情報</h2><p>ユーザーの氏名と生年月日を入力してください。</p></div></div>
        <div class="form-grid two-columns">
          <label>姓<span class="required">必須</span><input type="text" name="lastName" maxlength="36" autocomplete="family-name" required value="<c:out value='${enteredLastName}'/>"></label>
          <label>名<span class="required">必須</span><input type="text" name="firstName" maxlength="36" autocomplete="given-name" required value="<c:out value='${enteredFirstName}'/>"></label>
          <label class="full-width">生年月日<span class="required">必須</span><input type="date" name="birthday" min="1900-01-01" max="${currentDate}" required value="${enteredBirthday}"></label>
        </div>
      </section>

      <section><div class="section-heading"><span>2</span><div><h2>アカウント情報</h2><p>ログインに使用するIDと権限を設定します。</p></div></div>
        <div class="form-grid two-columns">
          <label>ユーザーID<span class="required">必須</span><input type="text" name="userId" minlength="4" maxlength="20" pattern="[A-Za-z][A-Za-z0-9_-]{3,19}" autocomplete="off" required value="<c:out value='${enteredUserId}'/>"><small>英字で始まる4～20文字の英数字・_・-</small></label>
          <label>権限<span class="required">必須</span><select name="permission" required><option value="">選択してください</option><c:forEach var="level" items="${permissionLevels}"><option value="${level.permissionId}" ${enteredPermission == level.permissionId ? 'selected' : ''}><c:out value="${level.permissionName}"/></option></c:forEach></select></label>
          <label class="full-width expiration-field">パスワード有効期限<span class="required">必須</span><input type="date" name="passwordExpiration" min="${currentDate}" required value="${not empty enteredExpiration ? enteredExpiration : defaultExpiration}"><small>期限を過ぎるとログインできなくなります。</small></label>
        </div>
      </section>

      <section><div class="section-heading"><span>3</span><div><h2>初期パスワード</h2><p>パスワードは暗号化して保存され、登録後に画面へ表示されません。</p></div></div>
        <div class="form-grid two-columns">
          <label>パスワード<span class="required">必須</span><div class="password-field"><input id="password" type="password" name="password" minlength="8" maxlength="72" autocomplete="new-password" required><button type="button" class="visibility-button" data-target="password">表示</button></div><small>英字と数字を含む8～72文字</small></label>
          <label>パスワード（確認）<span class="required">必須</span><div class="password-field"><input id="passwordConfirmation" type="password" name="passwordConfirmation" minlength="8" maxlength="72" autocomplete="new-password" required><button type="button" class="visibility-button" data-target="passwordConfirmation">表示</button></div></label>
        </div>
      </section>
      <div class="submit-area"><p>入力内容を確認して登録してください。</p><button class="primary-button" type="submit">ユーザーを登録</button></div>
    </form>
  </main>
  <script>
    document.querySelectorAll('.visibility-button').forEach(button=>button.addEventListener('click',()=>{const input=document.getElementById(button.dataset.target);const show=input.type==='password';input.type=show?'text':'password';button.textContent=show?'隠す':'表示';}));
  </script>
</body>
</html>
