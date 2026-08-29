const form = document.getElementById("chat-form");
const messageInput = document.getElementById("commentText");

form.addEventListener("submit", (e) => {
    e.preventDefault();   // 画面遷移を止める
    sendMessage();
});

messageInput.addEventListener("keydown", (e) => {
    // 日本語変換中のEnterは送信として扱わない。
    if (e.key === "Enter" && !e.shiftKey && !e.isComposing) {
        e.preventDefault();
        form.requestSubmit();
    }
});


async function sendMessage() {
    try {
        const sendMsg = messageInput.value;
        const roomId = document.getElementById("roomId").value;

        const response = await fetch(contextPath + "/StartChat", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: "commentText=" + encodeURIComponent(sendMsg)
                    + "&roomId=" + encodeURIComponent(roomId)
        });

        if (!response.ok) {
            throw new Error("HTTP Error : " + response.status);
        }

        const result = await response.json();

        drawChat(result);

        messageInput.value = "";
    } catch (e) {
        console.error(e);
    }
}

function drawChat(messages, scrollToBottom = true, preservedScrollTop = 0) {

    const chatArea = document.getElementById("chat-area");

    // 一旦クリア
    chatArea.innerHTML = "";

    messages.forEach(msg => {

        const isMine = msg.postedById === loginUserId;

        const html = `
            <div class="chat-message ${isMine ? "my-message" : "other-message"}">

                ${!isMine ?
                    `<div class="message-user">${escapeHtml(msg.postedByName)} ${msg.postedByAccountType === "AI" ? '<span class="ai-user-badge">AI</span>' : ''}</div>`
                    : ""}

                <div class="message-body">
                    ${escapeHtml(msg.message)}
                </div>

                <div class="message-time">
                    ${msg.createdAt}
                </div>

            </div>
        `;

        chatArea.insertAdjacentHTML("beforeend", html);

    });

    if (scrollToBottom) {
        chatArea.scrollTop = chatArea.scrollHeight;
    } else {
        chatArea.scrollTop = preservedScrollTop;
    }
}

function escapeHtml(value) {
    const div = document.createElement("div");
    div.textContent = value == null ? "" : value;
    return div.innerHTML;
}

async function refreshMessages() {
    if (document.hidden) return;
    try {
        const roomId = document.getElementById("roomId").value;
        const response = await fetch(contextPath + "/ChatMessages?roomId=" + encodeURIComponent(roomId));
        if (response.ok) {
            const messages = await response.json();
            // 通信中に利用者がスクロールすることもあるため、描画直前の位置を使う。
            const chatArea = document.getElementById("chat-area");
            const previousScrollTop = chatArea.scrollTop;
            const distanceFromBottom = chatArea.scrollHeight - chatArea.clientHeight - previousScrollTop;
            const wasNearBottom = distanceFromBottom <= 80;
            drawChat(messages, wasNearBottom, previousScrollTop);
        }
    } catch (e) {
        console.debug("chat refresh skipped", e);
    }
}

setInterval(refreshMessages, 3000);
