const form = document.getElementById("chat-form");

form.addEventListener("submit", (e) => {
    e.preventDefault();   // 画面遷移を止める
    sendMessage();
});


async function sendMessage() {
    try {
        const sendMsg = document.getElementById("commentText").value;

        const response = await fetch("StartChat", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: "commentText=" + encodeURIComponent(sendMsg)
        });

        if (!response.ok) {
            throw new Error("HTTP Error : " + response.status);
        }

        const result = await response.json();

        console.log(result);

    } catch (e) {
        console.error(e);
    }
}