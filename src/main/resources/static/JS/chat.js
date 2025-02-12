let selectedUserId = null;

// ✅ Get senderID from session when page loads
let senderID = null;
fetch("/api/session-user")
    .then(response => response.json())
    .then(data => {
        senderID = data.userID;
    })
    .catch(error => console.error("Error fetching senderID:", error));

document.addEventListener("DOMContentLoaded", function () {
    loadUsers();
});

// ✅ Scroll chat to the bottom on new messages
function scrollToBottom() {
    const chatMessages = document.getElementById("chatMessages");
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

// ✅ Load users dynamically
function loadUsers() {
    fetch("/api/users")
        .then(response => response.json())
        .then(users => {
            const userList = document.getElementById("userList");
            userList.innerHTML = "";
            users.forEach(user => {
                const userItem = document.createElement("li");
                const profileImg = document.createElement("img");

                // Set profile image
                profileImg.src = "/images/" + user.profile;
                profileImg.alt = user.username;
                profileImg.className = "profile-pic";

                userItem.textContent = user.username;
                userItem.prepend(profileImg);

                userItem.onclick = function () {
                    selectUser(user.userID, user.username);
                };
                userList.appendChild(userItem);
            });
        })
        .catch(error => console.error("Error loading users:", error));
}

// ✅ Select user for chat
function selectUser(userId, username) {
	if (!userId || isNaN(userId)) {
	    console.error("Error: Invalid userId received.");
	    return;
	}


    selectedUserId = userId;
    document.getElementById("chatHeader").textContent = "Chat with " + username;
    
    fetchMessages();
}

// ✅ Auto-select receiver if found in URL
const urlParams = new URLSearchParams(window.location.search);
const receiverID = urlParams.get("receiverID");

if (receiverID) {
    fetch(`/api/users/${receiverID}`)
        .then(response => response.json())
        .then(user => {
            selectUser(user.userID, user.username);
        })
        .catch(error => console.error("Error fetching user details:", error));
}


// ✅ Handle Image Upload
document.getElementById("imageInput").addEventListener("change", function (event) {
    const file = event.target.files[0];
    if (file) {
        uploadImage(file);
    }
});

// ✅ Upload Image
function uploadImage(file) {
    if (!selectedUserId || !senderID) {
        console.error("Error: User not selected or session expired.");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("receiverID", selectedUserId);

    fetch("/api/messages/upload", {
        method: "POST",
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            fetchMessages();
        } else {
            console.error("Error uploading image:", data.error);
        }
    })
    .catch(error => console.error("Error:", error));
}

// ✅ Fetch Messages
function fetchMessages() {
    if (!senderID || !selectedUserId) {
        console.error("Error: senderID or receiverID is missing.");
        return;
    }

    fetch(`/api/messages?receiverID=${selectedUserId}`)
        .then(response => response.json())
        .then(messages => {
            const chatMessages = document.getElementById("chatMessages");
            chatMessages.innerHTML = "";

            messages.forEach(msg => {
                const messageContainer = document.createElement("div");
                messageContainer.className = msg.sender.userID == senderID ? "message-container sent" : "message-container received";
                messageContainer.setAttribute("data-message-id", msg.messageID);

                if (msg.message) {
                    const messageText = document.createElement("div");
                    messageText.className = "message";
                    messageText.textContent = msg.message;
                    messageContainer.appendChild(messageText);
                }

                if (msg.imageUrl) {
                    const imgElement = document.createElement("img");
                    imgElement.src = `/Image/Chat/${msg.imageUrl}`; // ✅ Fixed Image Path
                    imgElement.className = "chat-image";
                    messageContainer.appendChild(imgElement);
                }

                chatMessages.appendChild(messageContainer);
            });

            scrollToBottom();
        })
        .catch(error => console.error("Error fetching messages:", error));
}

// ✅ Send Message
function sendMessage() {
    const messageInput = document.getElementById("messageInput");
    const message = messageInput.value.trim();

    if (!message || !selectedUserId || !senderID) {
        console.error("Error: Missing senderID, receiverID, or message.");
        return;
    }

    let chatMessage = {
        receiverID: selectedUserId,
        message: message
    };

    fetch("/api/messages", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(chatMessage)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            messageInput.value = "";
            fetchMessages();
        } else {
            console.error("Error saving message:", data.error);
        }
    })
    .catch(error => console.error("Error sending message:", error));
}
