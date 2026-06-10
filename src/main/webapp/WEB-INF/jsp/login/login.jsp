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
    <h1 style="text-align: center">開発機</h1>
    <form
      action="${pageContext.request.contextPath}/Login?"
      method="post"
      name="form1"
      style="text-align: center"
    >
      <div style="padding-top: 85px; padding-right: 370px">
        <label>ユーザーID：</label>
      </div>
      <div style="padding-top: 135px; padding-right: 370px">
        <label>パスワード：</label>
      </div>
      <div class="search-box" style="top: 190px">
        <input
          id="userId"
          class="search-txt"
          type="text"
          name="userId"
          autocomplete="username"
        /><br />
      </div>
      <div class="search-box" style="top: 350px">
        <input
          class="search-txt"
          type="password"
          name="password"
          id="pass"
          maxlength="5"
          autocomplete="current-password"
        /><br />
      </div>
      <div style="padding-top: 70px">
        <input
          id="loginBtn"
          type="submit"
          class="search-btn2"
          value="ログイン"
          onclick="return checkForm();"
        />
      </div>
    </form>
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
