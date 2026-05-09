let personality = "helper", currentSessionId = crypto.randomUUID();
let chats = JSON.parse(localStorage.getItem("chats")) || {};

const el = id => document.getElementById(id);
const capitalize = s => s.charAt(0).toUpperCase() + s.slice(1);

function setPersonality(type, button) {
    if (personality === type) return;
    personality = type;
    document.querySelectorAll(".personality-btn").forEach(btn => btn.classList.toggle("active", btn === button));
    el("chat-title").innerText = `Chatta med Eric ${capitalize(type)}`;
    addSystemMessage(`Personlighet ändrad till Eric ${capitalize(type)}`);
    // Vi sparar inte här, vi låter nästa meddelande bära med sig den nya personligheten
}

function newChat() {
    currentSessionId = crypto.randomUUID();
    el("messages").innerHTML = "";
    addSystemMessage(`Ny chatt startad med Eric ${capitalize(personality)}`);
    saveChat();
}

async function sendMessage() {
    const input = el("message-input"), message = input.value.trim();
    if (!message) return;
    addMessage(message, "user");
    input.value = "";
    const thinkingId = addThinkingMessage();
    try {
        const response = await fetch("/api/v1/chat", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ personality, message, sessionId: currentSessionId })
        });
        const data = await response.json();
        removeMessage(thinkingId);
        addMessage(response.ok ? data.reply : (data.message || "Något gick fel."), "ai");
        saveChat();
    } catch (e) {
        removeMessage(thinkingId);
        addMessage("Något gick fel.", "ai");
        saveChat();
    }
}

function addMessage(text, type) {
    const messages = el("messages"), div = document.createElement("div");
    div.className = `message ${type === "user" ? "user-message" : "ai-message"}`;
    if (type === "user") div.innerText = text;
    else renderFormattedText(div, text);
    messages.appendChild(div);
    messages.scrollTop = messages.scrollHeight;
    return div.id;
}

function renderFormattedText(container, text) {
    text.split("```").forEach((part, i) => {
        if (i % 2 === 1) {
            const pre = document.createElement("pre"), code = document.createElement("code");
            pre.className = "code-block";
            let codeText = part;
            const firstNewline = part.indexOf("\n");
            if (firstNewline !== -1 && firstNewline < 15) codeText = part.substring(firstNewline + 1);
            code.innerHTML = highlightCode(codeText.trim());
            pre.appendChild(code);
            container.appendChild(pre);
        } else if (part.trim() || (i === 0 && text.includes("```"))) {
            const span = document.createElement("span");
            span.style.whiteSpace = "pre-wrap";
            span.innerText = part;
            container.appendChild(span);
        }
    });
}

function highlightCode(code) {
    let esc = code.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    esc = esc.replace(/\b(await|break|case|catch|class|const|continue|debugger|default|delete|do|else|enum|export|extends|false|finally|for|function|if|import|in|instanceof|new|null|return|super|switch|this|throw|true|try|typeof|var|void|while|with|yield|async)\b/g, '<span class="code-keyword">$1</span>');
    esc = esc.replace(/(["'])(?:(?=(\\?))\2.)*?\1/g, '<span class="code-string">$&</span>');
    esc = esc.replace(/\/\/.*/g, '<span class="code-comment">$&</span>').replace(/\/\*[\s\S]*?\*\//g, '<span class="code-comment">$&</span>');
    return esc.replace(/\b([a-zA-Z_]\w*)(?=\s*\()/g, '<span class="code-function">$1</span>');
}

function addSystemMessage(text) {
    const messages = el("messages"), div = document.createElement("div");
    div.className = "system-message";
    div.innerText = text;
    messages.appendChild(div);
    messages.scrollTop = messages.scrollHeight;
}

function addThinkingMessage() {
    const messages = el("messages"), div = document.createElement("div"), id = "thinking-" + Date.now();
    div.id = id;
    div.className = "message ai-message thinking-message";
    div.innerText = "Eric tänker.";
    messages.appendChild(div);
    messages.scrollTop = messages.scrollHeight;
    let dots = 1;
    div.dataset.intervalId = setInterval(() => {
        dots = (dots % 3) + 1;
        div.innerText = "Eric tänker" + ".".repeat(dots);
    }, 500);
    return id;
}

function removeMessage(id) {
    const e = el(id);
    if (e) {
        if (e.dataset.intervalId) clearInterval(e.dataset.intervalId);
        e.remove();
    }
}

function saveChat() {
    const userMsgs = document.querySelectorAll(".user-message");
    let title = userMsgs.length ? userMsgs[0].innerText : null;
    if (title && title.length > 25) title = title.substring(0, 25).trim() + "...";
    chats[currentSessionId] = {
        html: el("messages").innerHTML,
        personality: personality,
        title: (chats[currentSessionId] || {}).title || title
    };
    localStorage.setItem("chats", JSON.stringify(chats));
    loadHistory();
}

function loadHistory() {
    const list = el("history-list");
    list.innerHTML = "";
    Object.keys(chats).forEach(id => {
        const div = document.createElement("div");
        div.className = "chat-item";
        div.innerHTML = `<span style="flex:1; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">${chats[id].title || "Chatt " + id.substring(0, 8)}</span>
                         <button class="delete-chat-btn">Radera</button>`;
        div.onclick = () => openChat(id);
        div.querySelector("button").onclick = e => { e.stopPropagation(); deleteChat(id); };
        list.appendChild(div);
    });
}

function deleteChat(id) {
    if (!confirm("Vill du radera den här chatten?")) return;
    delete chats[id];
    localStorage.setItem("chats", JSON.stringify(chats));
    if (currentSessionId === id) {
        currentSessionId = crypto.randomUUID();
        el("messages").innerHTML = "";
        addSystemMessage("Chatten raderades. Ny chatt startad.");
        saveChat();
    }
    loadHistory();
}

function deleteAllChats() {
    if (!confirm("Vill du radera ALLA tidigare chatter?")) return;
    chats = {};
    localStorage.setItem("chats", JSON.stringify(chats));
    currentSessionId = crypto.randomUUID();
    el("messages").innerHTML = "";
    addSystemMessage("Alla chatter raderades. Ny chatt startad.");
    saveChat();
    loadHistory();
}

function openChat(id) {
    currentSessionId = id;
    const chat = chats[id];
    el("messages").innerHTML = chat.html || chat;
    if (chat.personality) {
        personality = chat.personality;
        el("chat-title").innerText = `Chatta med Eric ${capitalize(personality)}`;
        document.querySelectorAll(".personality-btn").forEach(btn => {
            const btnType = btn.getAttribute("onclick").match(/'([^']+)'/)[1];
            btn.classList.toggle("active", btnType === personality);
        });
    }
}

el("message-input").onkeypress = e => { if (e.key === "Enter") sendMessage(); };
loadHistory();