let selectedUserId = null;
let ws = null;
let tempChat = null;
let conversationsList = [];
let reconnectInterval = 3000; // Reconnect every 3 seconds
connectWebSocket();

const createNewConversationDialog = document.getElementById(
	"create_new_conversation_dialog"
);
const createNewConversationBtn = document.getElementById(
	"create_new_conversation_btn"
);
const userList = document.getElementById("userList");

const messageInput = document.getElementById("messageInput");

const searchInput = document.getElementById("search_user_input");

searchInput.addEventListener("input", function () {
	const searchUserList = document.getElementById("search_user_list");
	// Show loading state
	searchUserList.innerHTML = '<div class="loading">Searching users...</div>';
	searchUser(searchInput.value);
});

createNewConversationBtn.addEventListener("click", function () {
	createNewConversationDialog.showModal();
});

document.addEventListener("DOMContentLoaded", function () {
	loadUsers();
});

const senderIdInput = document.getElementById("senderId");
sessionStorage.setItem("user_id", senderIdInput.value);

function scrollToBottom() {
	const chatMessages = document.getElementById("chatMessages");
	chatMessages.scrollTop = chatMessages.scrollHeight;
}

const urlParams = new URLSearchParams(window.location.search);
const receiverId = urlParams.get("receiverID");
const receiverName = urlParams.get("receiverName");
if (receiverId && receiverName) {
	selectUser(receiverId, receiverName, "/Image/profile/default.png");

	const alreadyExist = conversationsList.some(
		(conversation) => conversation.user_id === receiverId
	);
	if (!alreadyExist) {
		console.log(conversationsList);
		conversationsList.unshift({
			user_id: receiverId,
			username: receiverName,
			userImage: null,
		});
		console.log(conversationsList);

		renderConversations(conversationsList);
	}
}

function loadUsers() {
	fetch("/api/messages/conversations")
		.then((response) => {
			if (response.status === 401) {
				window.location.href = "/loginPage";
				throw new Error("Unauthorized");
			}
			return response.json();
		})
		.then((users) => {
			if (receiverId && receiverName) {
				conversationsList = conversationsList.concat(users);
			} else {
				conversationsList = users;
			}
			renderConversations(conversationsList);
			console.log(conversationsList);
		})
		.catch((error) => {
			console.error("Error loading users:", error);
		});
}

function searchUser(searchInput) {
	const searchUserList = document.getElementById("search_user_list");
	fetch("/api/messages/search/users?username=" + searchInput)
		.then((response) => response.json())
		.then((users) => {
			console.log(users);
			searchUserList.innerHTML = "";
			if (users.length === 0) {
				searchUserList.innerHTML =
					'<div class="no-results">No users found</div>';
				return;
			}
			users.forEach((user) => {
				const userItem = document.createElement("li");
				const profileImg = document.createElement("img");
				const userInfoDiv = document.createElement("div");
				const btn = document.createElement("button");
				btn.innerHTML = "<i class='fas fa-paper-plane'></i>";
				// Set profile image
				const userImage = user.profile
					? "/Image/profile/" + user.profile
					: "/Image/profile/default.png";
				profileImg.src = userImage;
				profileImg.alt = user.username;
				profileImg.className = "profile-pic";

				userItem.classList.add("search_user_item");
				userInfoDiv.textContent = user.username;
				userInfoDiv.prepend(profileImg);
				userItem.appendChild(userInfoDiv);
				userItem.appendChild(btn);
				userItem.onclick = function () {
					selectUser(user.user_id, user.username, userImage);
					const alreadyExist = conversationsList.some(
						(conversation) => conversation.user_id === user.user_id
					);
					if (!alreadyExist) {
						conversationsList.unshift(user);
						renderConversations(conversationsList);
					}
					createNewConversationDialog.close();
				};
				searchUserList.appendChild(userItem);
			});
		});
}

function selectUser(userId, username, userImage) {
	if (!userId) {
		console.error("Error: selectUser received an invalid userId.");
		return;
	}
	const chatHeader = document.getElementById("chatHeader");

	selectedUserId = userId;
	chatHeader.style.display = "block";
	const profileImg = document.createElement("img");
	const profileDiv = document.createElement("div");
	profileDiv.classList.add("selected-user-profile");

	profileImg.src = userImage;
	profileImg.alt = username;
	profileImg.className = "profile-pic";

	chatHeader.innerHTML = "";
	profileDiv.innerHTML = `<h3>${username}</h3> `;
	profileDiv.prepend(profileImg);
	chatHeader.appendChild(profileDiv);

	chatHeader.classList.replace(
		"chat-header-without-selected-user",
		"chat-header"
	);
	messageInput.focus();
	document.getElementById("chat-input").style.opacity = "100";
	connectWebSocket();
	fetchMessages();
}

document
	.getElementById("imageInput")
	.addEventListener("change", function (event) {
		const file = event.target.files[0];
		if (file) {
			uploadImage(file);
		}
	});

function uploadImage(file) {
	const senderID = sessionStorage.getItem("user_id");
	if (!selectedUserId || !senderID) {
		console.error("Error: User not selected or session expired.");
		return;
	}

	const formData = new FormData();
	formData.append("file", file);
	formData.append("senderID", senderID);
	formData.append("receiverID", selectedUserId);
	let chatMessage = {
		senderID: senderID,
		receiverID: selectedUserId, // ✅ Send message only to selectedUserId
		message: "message",
	};

	fetch("/api/messages/upload", {
		method: "POST",
		body: formData,
	})
		.then((response) => response.json())
		.then((data) => {
			if (data.success) {
				if (ws && ws.readyState === WebSocket.OPEN) {
					ws.send(JSON.stringify(chatMessage));
				}

				fetchMessages();
			} else {
				console.error("Error uploading image:", data.error);
			}
		})
		.catch((error) => console.error("Error:", error));
}

function fetchMessages() {
	const senderID = sessionStorage.getItem("user_id");

	if (!senderID) {
		console.error("Error: senderID is missing from sessionStorage.");
		return;
	}

	if (!selectedUserId) {
		console.error("Error: receiverID is undefined. Cannot fetch messages.");
		return;
	}

	fetch("/api/messages?senderID=" + senderID + "&receiverID=" + selectedUserId)
		.then((response) => response.json())
		.then((messages) => {
			const chatMessages = document.getElementById("chatMessages");
			chatMessages.innerHTML = "";

			messages.forEach((msg) => {
				const messageContainer = document.createElement("div");
				messageContainer.className =
					msg.senderID == senderID
						? "message-container sent"
						: "message-container received";
				messageContainer.setAttribute("data-message-id", msg.messageID);

				const buttonContainer = document.createElement("div");
				buttonContainer.className = "button-container";

				// ✅ Add message text if available
				if (msg.message) {
					const messageText = document.createElement("div");
					messageText.className = "message";
					messageText.textContent = msg.message;
					messageContainer.appendChild(messageText);
				}

				// ✅ Add image if available
				if (msg.imageUrl) {
					const imgElement = document.createElement("img");
					imgElement.src = "/uploads/" + msg.imageUrl; // ✅ Adjusted image path
					imgElement.className = "chat-image";
					messageContainer.appendChild(imgElement);
				}

				// ✅ Add Edit and Delete buttons for text messages
				if (msg.message && msg.senderID == senderID) {
					const editButton = document.createElement("button");
					editButton.textContent = "✏️";
					editButton.className = "edit-btn";
					editButton.onclick = function () {
						enableEditMode(msg.messageID, msg.message);
					};

					const deleteButton = document.createElement("button");
					deleteButton.textContent = "🗑";
					deleteButton.className = "delete-btn";
					deleteButton.onclick = function () {
						deleteMessage(msg.messageID);
					};

					buttonContainer.appendChild(editButton);
					buttonContainer.appendChild(deleteButton);
				}

				// ✅ Append buttons (only for text messages)
				if (msg.message) {
					messageContainer.appendChild(buttonContainer);
				}

				chatMessages.appendChild(messageContainer);
			});

			scrollToBottom();
		})
		.catch((error) => console.error("Error fetching messages:", error));
}

function connectWebSocket() {
	if (ws && ws.readyState === WebSocket.OPEN) {
		console.log("WebSocket is already connected.");
		return;
	}

	console.log("Connecting to WebSocket...");
	ws = new WebSocket("ws://localhost:8080/chat");

	ws.onopen = function () {
		console.log("WebSocket connected.");
	};

	ws.onmessage = function (event) {
		console.log("Received WebSocket message:", event.data);
		/* const chatMessages = document.getElementById("chatMessages");
                const messageElement = document.createElement("div");
                messageElement.className = "message-container received";
                messageElement.textContent = event.data;
                chatMessages.appendChild(messageElement);*/
		loadUsers();
		fetchMessages();
		scrollToBottom();
	};

	ws.onclose = function (event) {
		console.warn(
			"WebSocket closed. Code: " + event.code + " Reason: " + event.reason
		);

		if (event.code !== 1000) {
			console.warn(
				"Reconnecting in " + reconnectInterval / 1000 + " seconds..."
			);
			setTimeout(connectWebSocket, reconnectInterval);
		}
	};

	ws.onerror = function (error) {
		console.error("WebSocket error:", error);
	};
}

function sendMessage() {
	const message = messageInput.value.trim();
	const senderID = sessionStorage.getItem("user_id");
	let chatMessage = {
		senderID: senderID,
		receiverID: selectedUserId, // ✅ Send message only to selectedUserId
		message: message,
	};

	if (message && selectedUserId) {
		fetch("/api/messages", {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(chatMessage),
		})
			.then((response) => response.json())
			.then((data) => {
				if (data.success) {
					messageInput.value = "";

					if (ws && ws.readyState === WebSocket.OPEN) {
						ws.send(JSON.stringify(chatMessage));
					}

					fetchMessages();
				} else {
					console.error("Error saving message:", data.error);
				}
			})
			.catch((error) => console.error("Error sending message:", error));
	}
}

function enableEditMode(messageID, originalText) {
	const messageContainer = document.querySelector(
		`[data-message-id='${messageID}']`
	);
	if (!messageContainer) {
		console.error("Message element not found for ID:", messageID);
		return;
	}

	messageContainer.innerHTML = "";

	const inputBox = document.createElement("input");
	inputBox.type = "text";
	inputBox.value = originalText;
	inputBox.className = "edit-input";

	const saveButton = document.createElement("button");
	saveButton.textContent = "💾 Save";
	saveButton.className = "save-btn";
	saveButton.onclick = function () {
		saveUpdatedMessage(messageID, inputBox.value);
	};

	messageContainer.appendChild(inputBox);
	messageContainer.appendChild(saveButton);
}

function saveUpdatedMessage(messageID, updatedText) {
	fetch("/api/messages", {
		method: "PUT",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify({ messageID: messageID, updatedMessage: updatedText }),
	})
		.then((response) => response.json())
		.then((data) => {
			if (data.success) {
				console.log("Message updated successfully");
				fetchMessages();
			} else {
				console.error("Error updating message:", data.error);
			}
		})
		.catch((error) => console.error("Error updating message:", error));
}

function deleteMessage(messageID) {
	fetch("/api/messages?messageID=" + messageID, { method: "DELETE" })
		.then((response) => response.json())
		.then((data) => {
			if (data.success) {
				fetchMessages();
			} else {
				console.error("Error deleting message:", data.error);
			}
		})
		.catch((error) => console.error("Error deleting message:", error));
}

function renderConversations(users) {
	userList.innerHTML = "";
	users.forEach((user) => {
		const userItem = document.createElement("li");
		const profileImg = document.createElement("img");

		// Set profile image
		const userImage = user.profile
			? "/Image/profile/" + user.profile
			: "/Image/profile/default.png";

		profileImg.src = userImage;
		profileImg.alt = user.username;
		profileImg.className = "profile-pic";

		userItem.textContent = user.username;
		userItem.prepend(profileImg);

		userItem.onclick = function () {
			selectUser(user.user_id, user.username, userImage);
		};
		userList.appendChild(userItem);
	});
}
