<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ja">
    <head>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <title>設計リバース | NW Project</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/reverse-engineering.css" />
        <script defer src="${pageContext.request.contextPath}/js/reverse-engineering.js"></script>
    </head>
    <body>
        <jsp:include page="/WEB-INF/jsp/common/header.jsp" />
        <main class="reverse-page" data-endpoint="${pageContext.request.contextPath}/ReverseEngineering">
            <header class="reverse-header">
                <div>
                    <p class="reverse-eyebrow">SYSTEM DESIGN</p>
                    <h1>設計リバース</h1>
                    <p>稼働中のDBスキーマと配備済みJavaクラスの構造を読み取り専用で確認します。</p>
                </div>
                <a href="${pageContext.request.contextPath}/BusinessMenu">業務メニューへ戻る</a>
            </header>

            <div class="reverse-tabs" role="tablist" aria-label="解析対象">
                <button type="button" role="tab" aria-selected="true" data-view="database">DBリバース</button>
                <button type="button" role="tab" aria-selected="false" data-view="java">Javaリバース</button>
            </div>

            <section class="reverse-toolbar" aria-label="表示条件">
                <label>検索 <input id="reverse-search" type="search" placeholder="テーブル名またはクラス名" /></label>
                <span id="reverse-count" aria-live="polite"></span>
                <button id="reverse-export" type="button">構造をJSONで保存</button>
            </section>

            <p id="reverse-status" class="reverse-status" role="status">構造を読み込んでいます…</p>
            <div class="reverse-layout" id="reverse-layout" hidden>
                <aside class="reverse-list-panel">
                    <h2 id="reverse-list-title">テーブル</h2>
                    <div id="reverse-list" class="reverse-list"></div>
                </aside>
                <section class="reverse-main-panel">
                    <div id="reverse-detail"></div>
                    <section class="reverse-diagram-panel">
                        <h2>関係図</h2>
                        <p id="reverse-diagram-caption"></p>
                        <svg id="reverse-diagram" role="img" aria-label="選択した項目の関係図"></svg>
                        <div id="reverse-relations" class="reverse-relations"></div>
                    </section>
                </section>
            </div>
            <p class="reverse-note">
                DBは外部キー制約、Javaは継承・実装・フィールド・公開メソッドの型参照を表示します。外部キーが定義されていない列やメソッド本体の呼び出し関係は推測しません。
            </p>
        </main>
    </body>
</html>
