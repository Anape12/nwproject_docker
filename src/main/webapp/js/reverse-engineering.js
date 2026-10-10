(function () {
    "use strict";

    const root = document.querySelector(".reverse-page");
    if (!root) return;

    const endpoint = root.dataset.endpoint;
    const search = document.getElementById("reverse-search");
    const status = document.getElementById("reverse-status");
    const layout = document.getElementById("reverse-layout");
    const list = document.getElementById("reverse-list");
    const detail = document.getElementById("reverse-detail");
    const count = document.getElementById("reverse-count");
    const diagram = document.getElementById("reverse-diagram");
    const caption = document.getElementById("reverse-diagram-caption");
    const relationList = document.getElementById("reverse-relations");
    const cache = {};
    let view = "database";
    let selected = null;

    function element(tag, text, className) {
        const node = document.createElement(tag);
        if (text != null) node.textContent = text;
        if (className) node.className = className;
        return node;
    }

    function svgElement(tag, attributes, text) {
        const node = document.createElementNS("http://www.w3.org/2000/svg", tag);
        Object.entries(attributes).forEach(([key, value]) => node.setAttribute(key, String(value)));
        if (text != null) node.textContent = text;
        return node;
    }

    function nameOf(item) {
        return item.name;
    }

    function items() {
        return view === "database" ? cache.database.tables : cache.java.classes;
    }

    function shortName(name) {
        return view === "java" ? name.substring(name.lastIndexOf(".") + 1) : name;
    }

    function showError(message) {
        status.textContent = message;
        status.classList.add("error");
        status.hidden = false;
        layout.hidden = true;
    }

    async function load(nextView) {
        view = nextView;
        selected = null;
        search.value = "";
        document.querySelectorAll(".reverse-tabs button").forEach((button) => {
            button.setAttribute("aria-selected", String(button.dataset.view === view));
        });
        document.getElementById("reverse-list-title").textContent = view === "database" ? "テーブル" : "クラス";
        status.textContent = "構造を読み込んでいます…";
        status.classList.remove("error");
        status.hidden = false;
        layout.hidden = true;
        try {
            if (!cache[view]) {
                const url = new URL(endpoint, window.location.href);
                url.searchParams.set("view", view);
                const response = await fetch(url, { credentials: "same-origin" });
                if (!response.ok) throw new Error("HTTP " + response.status);
                cache[nextView] = await response.json();
            }
            if (view !== nextView) return;
            status.hidden = true;
            layout.hidden = false;
            renderList();
        } catch (error) {
            if (view !== nextView) return;
            showError("構造を取得できませんでした（" + error.message + "）。再読み込みしてください。");
        }
    }

    function renderList() {
        if (!cache[view]) return;
        const term = search.value.trim().toLocaleLowerCase();
        const filtered = items().filter((item) => {
            if (nameOf(item).toLocaleLowerCase().includes(term)) return true;
            return view === "database"
                ? item.columns.some((column) => column.name.toLocaleLowerCase().includes(term))
                : item.packageName.toLocaleLowerCase().includes(term);
        });
        count.textContent = filtered.length + " / " + items().length + " 件";
        if (!filtered.some((item) => nameOf(item) === selected)) selected = filtered[0]?.name || null;
        list.replaceChildren();
        filtered.forEach((item) => {
            const button = element("button", view === "java" ? item.simpleName : item.name);
            button.type = "button";
            button.title = item.name;
            button.classList.toggle("active", item.name === selected);
            button.addEventListener("click", () => {
                selected = item.name;
                renderList();
            });
            list.append(button);
        });
        if (!filtered.length) list.append(element("p", "一致する項目はありません。"));
        renderDetail();
    }

    function renderDetail() {
        detail.replaceChildren();
        const item = items().find((entry) => entry.name === selected);
        if (!item) {
            detail.append(element("p", "項目を選択してください。"));
            diagram.replaceChildren();
            relationList.replaceChildren();
            return;
        }
        if (view === "database") renderTable(item);
        else renderClass(item);
        renderDiagram(item);
    }

    function renderTable(table) {
        detail.append(element("h2", table.name));
        detail.append(element("p", table.columns.length + " 列", "reverse-subtitle"));
        const wrap = element("div", null, "reverse-table-wrap");
        const grid = element("table", null, "reverse-table");
        const head = element("thead");
        const titles = element("tr");
        ["列名", "型", "NULL", "キー", "デフォルト", "備考"].forEach((title) => titles.append(element("th", title)));
        head.append(titles);
        grid.append(head);
        const body = element("tbody");
        table.columns.forEach((column) => {
            const row = element("tr");
            row.append(element("td", column.name));
            row.append(element("td", column.type + (column.size ? " (" + column.size + ")" : "")));
            row.append(element("td", column.nullable ? "可" : "不可"));
            const key = element("td");
            if (column.primaryKey) key.append(element("span", "PK", "reverse-badge"));
            row.append(key);
            row.append(element("td", column.defaultValue == null ? "—" : String(column.defaultValue)));
            row.append(element("td", column.remarks || "—"));
            body.append(row);
        });
        grid.append(body);
        wrap.append(grid);
        detail.append(wrap);
    }

    function renderClass(type) {
        detail.append(element("span", type.kind, "reverse-kind"));
        detail.append(element("h2", type.simpleName));
        detail.append(element("p", type.name, "reverse-subtitle"));
        detail.append(element("h3", "フィールド"));
        const fields = element("ul", null, "reverse-code-list");
        type.fields.forEach((field) => fields.append(element("li", field.name + " : " + field.type)));
        if (!type.fields.length) fields.append(element("li", "表示対象なし"));
        detail.append(fields);
        detail.append(element("h3", "公開メソッド"));
        const methods = element("ul", null, "reverse-code-list");
        type.methods.forEach((method) => methods.append(element("li", method)));
        if (!type.methods.length) methods.append(element("li", "表示対象なし"));
        detail.append(methods);
    }

    function relationships(name) {
        return view === "database"
            ? cache.database.relations.filter((relation) => relation.fromTable === name || relation.toTable === name)
            : cache.java.relations.filter((relation) => relation.from === name || relation.to === name);
    }

    function ends(relation) {
        return view === "database" ? [relation.fromTable, relation.toTable] : [relation.from, relation.to];
    }

    function description(relation) {
        return view === "database"
            ? relation.fromTable +
                  "." +
                  relation.fromColumn +
                  " → " +
                  relation.toTable +
                  "." +
                  relation.toColumn +
                  " (FK)"
            : shortName(relation.from) + " → " + shortName(relation.to) + " (" + relation.kind + ")";
    }

    function renderDiagram(item) {
        diagram.replaceChildren();
        relationList.replaceChildren();
        const relations = relationships(item.name);
        caption.textContent = relations.length
            ? relations.length + " 件の定義済み関係"
            : "定義済みの関係はありません。";
        relations.forEach((relation) => relationList.append(element("span", description(relation))));

        const neighbours = [
            ...new Set(
                relations.map((relation) => {
                    const [from, to] = ends(relation);
                    return from === item.name ? to : from;
                })
            ),
        ].slice(0, 8);
        const width = 760;
        const height = neighbours.length ? Math.max(250, neighbours.length * 58 + 40) : 200;
        diagram.setAttribute("viewBox", "0 0 " + width + " " + height);
        const centerY = height / 2;
        if (!neighbours.length) {
            drawNode(230, 76, 300, 48, shortName(item.name), true);
            return;
        }
        neighbours.forEach((name, index) => {
            const y = 28 + index * 58;
            diagram.append(
                svgElement("line", { x1: 430, y1: centerY, x2: 545, y2: y + 20, stroke: "#94a3b8", "stroke-width": 2 })
            );
            drawNode(545, y, 195, 40, shortName(name), false);
        });
        drawNode(80, centerY - 26, 350, 52, shortName(item.name), true);
        if (relations.length > neighbours.length) {
            diagram.append(
                svgElement(
                    "text",
                    { x: 550, y: height - 7, fill: "#667085", "font-size": 12 },
                    "詳細な関係は下の一覧を参照"
                )
            );
        }
    }

    function drawNode(x, y, width, height, label, active) {
        diagram.append(
            svgElement("rect", {
                x,
                y,
                width,
                height,
                rx: 9,
                fill: active ? "#3157d5" : "#fff",
                stroke: active ? "#3157d5" : "#cbd5e1",
            })
        );
        const clipped = label.length > 33 ? label.substring(0, 30) + "…" : label;
        const text = svgElement(
            "text",
            {
                x: x + 12,
                y: y + height / 2 + 5,
                fill: active ? "#fff" : "#344054",
                "font-size": 13,
                "font-weight": active ? 700 : 500,
            },
            clipped
        );
        text.append(svgElement("title", {}, label));
        diagram.append(text);
    }

    document.querySelectorAll(".reverse-tabs button").forEach((button) => {
        button.addEventListener("click", () => load(button.dataset.view));
    });
    search.addEventListener("input", renderList);
    document.getElementById("reverse-export").addEventListener("click", () => {
        if (!cache[view]) return;
        const blob = new Blob([JSON.stringify(cache[view], null, 2)], { type: "application/json;charset=utf-8" });
        const url = URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        link.download = "nwproject-" + view + "-reverse.json";
        link.click();
        setTimeout(() => URL.revokeObjectURL(url), 1000);
    });
    load("database");
})();
