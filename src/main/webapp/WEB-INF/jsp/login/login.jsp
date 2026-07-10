<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <% %>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <link
      rel="stylesheet"
      href="${pageContext.request.contextPath}/css/style11.css"
    />
    <script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
    <script>
      jQuery(window).on("load", function () {
        jQuery("#loader-bg").hide();
      });
    </script>
    <title>SampleMenu</title>
  </head>
  <body>
    <div class="login-title">
      <h1>NW Project</h1>
      <p>Management System</p>
    </div>

    <div class="login-card">
      <form action="${pageContext.request.contextPath}/Login" method="post">
        <div class="form-group">
          <label>ユーザーID</label>
          <input type="text" name="userId" autocomplete="username">
        </div>

        <div class="form-group">
          <label>パスワード</label>
          <input type="password" name="password" autocomplete="current-password">
        </div>

        <input class="login-btn" id="loginBtn" type="submit" value="ログイン" onclick="return checkForm();">
      </form>
    </div>
    <script
      type="text/javascript"
      src="${pageContext.request.contextPath}/js/Login.js"
    ></script>
  </body>
  <div
    style="position: absolute; bottom: 0; padding-left: 570px"
    class="fotter"
  >
    <jsp:include page="footer.jsp" flush="true" />
  </div>
</html>
