const form = document.getElementById("chat-form");

form.addEventListener("submit", (e) => {
    e.preventDefault();   // 画面遷移を止める
    sendMessage();
});


async function sendMessage() {
    try {
        const sendMsg = document.getElementById("commentText").value;
        const roomId = document.getElementById("roomId").value;

        const response = await fetch("StartChat", {
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

        document.getElementById("commentText").value = "";
    } catch (e) {
        console.error(e);
    }
}

function drawChat(messages) {

    const chatArea = document.getElementById("chat-area");

    // 一旦クリア
    chatArea.innerHTML = "";

    messages.forEach(msg => {

        const isMine = msg.postedById === loginUserId;

        const html = `
            <div class="chat-message ${isMine ? "my-message" : "other-message"}">

                ${!isMine ?
                    `<div class="message-user">${msg.postedByName}</div>`
                    : ""}

                <div class="message-body">
                    ${msg.message}
                </div>

                <div class="message-time">
                    ${msg.createdAt}
                </div>

            </div>
        `;

        chatArea.insertAdjacentHTML("beforeend", html);

    });

    // 一番下までスクロール
    chatArea.scrollTop = chatArea.scrollHeight;
}