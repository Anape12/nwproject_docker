<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String result = (String) session.getAttribute("resultApp"); %>
<!DOCTYPE html>
<html lang="ja">
    <head>
        <meta charset="UTF-8" />
        <title>メインメニュー（<%= result %>）</title>
        <script src="https://code.jquery.com/jquery-1.11.3.min.js"></script>
        <link
            href="https://cdn-na.infragistics.com/igniteui/2019.2/latest/css/themes/infragistics/infragistics.theme.css"
            rel="stylesheet"
        />
        <link
            href="https://cdn-na.infragistics.com/igniteui/2019.2/latest/css/structure/infragistics.css"
            rel="stylesheet"
        />
        <script src="https://cdn-na.infragistics.com/igniteui/2019.2/latest/js/infragistics.core.js"></script>
        <script src="https://cdn-na.infragistics.com/igniteui/2019.2/latest/js/infragistics.lob.js"></script>
    </head>
    <body style="background: #63515f">
        <div style="width: 200px; margin-top: 30px"></div>
        <div style="text-align: center; width: 400px">
            <p>選択アプリ【<%= result %>】</p>
            <form
                method="post"
                enctype="multipart/form-data"
                action="${pageContext.request.contextPath}/UploadServlet"
                onsubmit="return requestServe()"
            >
                <input type="file" name="FILE_INFO" /><br />
                <input type="submit" value="Upload" name="Upload" />
            </form>
            <p><button class="search-btn2" onclick="history.back()">ログアウト</button></p>
            <jsp:include page="../footer.jsp" flush="true" />
        </div>
        <table id="grid"></table>
        <script>
            $(function () {
                const data = [
                    { Name: "a0001", Age: 99 },
                    { Name: "a0002", Age: 99 },
                    { Name: "a0003", Age: 99 },
                    { Name: "a0004", Age: 99 },
                    { Name: "a0005", Age: 99 },
                ];
                $("#grid").igGrid({ dataSource: data });
            });
        </script>
        <script src="${pageContext.request.contextPath}/js/RequestJax.js"></script>
    </body>
</html>
