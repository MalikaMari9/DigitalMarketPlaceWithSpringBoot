let selectedUserId = null;
        let ws = null;
        let reconnectInterval = 3000; // Reconnect every 3 seconds

        document.addEventListener("DOMContentLoaded", function () {
            loadUsers();
        });
		
        
        function scrollToBottom() {
            const chatMessages = document.getElementById("chatMessages");
            chatMessages.scrollTop = chatMessages.scrollHeight;
        }

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
                        const userImage = "/images/" + user.profile;
                        profileImg.src = userImage;
                        profileImg.alt = user.username;
                        profileImg.className = "profile-pic";

                        userItem.textContent = user.username;
                        userItem.prepend(profileImg);

                        userItem.onclick = function () {
                            selectUser(user.user_id, user.username);
                        };
                        userList.appendChild(userItem);
                    });
                })
                .catch(error => console.error("Error loading users:", error));
        }

        function selectUser(userId, username) {

            if (!userId) {
                console.error("Error: selectUser received an invalid userId.");
                return;
            }

            selectedUserId = userId;
            document.getElementById("chatHeader").textContent = "Chat with " + username;
            
            connectWebSocket();
            fetchMessages();
        }

		document.getElementById("imageInput").addEventListener("change", function (event) {
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
		        .then(response => response.json())
		        .then(messages => {
		            const chatMessages = document.getElementById("chatMessages");
		            chatMessages.innerHTML = "";

		            messages.forEach(msg => {
		                const messageContainer = document.createElement("div");
		                messageContainer.className = msg.senderID == senderID ? "message-container sent" : "message-container received";
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
		        .catch(error => console.error("Error fetching messages:", error));
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
                const chatMessages = document.getElementById("chatMessages");
                const messageElement = document.createElement("div");
                messageElement.className = "message-container received";
                messageElement.textContent = event.data;
                chatMessages.appendChild(messageElement);
                
                scrollToBottom();

            };

            ws.onclose = function (event) {
                console.warn("WebSocket closed. Code: " + event.code + " Reason: " + event.reason);
                
                if (event.code !== 1000) {  
                    console.warn("Reconnecting in " + (reconnectInterval / 1000) + " seconds...");
                    setTimeout(connectWebSocket, reconnectInterval);
                }
            };

            ws.onerror = function (error) {
                console.error("WebSocket error:", error);
            };
        }

        function sendMessage() {
            const messageInput = document.getElementById("messageInput");
            const message = messageInput.value.trim();
            const senderID = sessionStorage.getItem("user_id");
			let chatMessage = {
			        senderID: senderID,
			        receiverID: selectedUserId, // ✅ Send message only to selectedUserId
			        message: message
			    };

            if (message && selectedUserId) {
                fetch("/api/messages", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
					body: JSON.stringify(chatMessage)

                })
                .then(response => response.json())
                .then(data => {
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
                .catch(error => console.error("Error sending message:", error));
            }
        }

        function enableEditMode(messageID, originalText) {
            const messageContainer = document.querySelector(`[data-message-id='${messageID}']`);
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
                body: JSON.stringify({ messageID: messageID, updatedMessage: updatedText })
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    console.log("Message updated successfully");
                    fetchMessages();
                } else {
                    console.error("Error updating message:", data.error);
                }
            })
            .catch(error => console.error("Error updating message:", error));
        }
        
        function deleteMessage(messageID) {
            fetch("/api/messages?messageID=" + messageID, { method: "DELETE" })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        fetchMessages();
                    } else {
                        console.error("Error deleting message:", data.error);
                    }
                })
                .catch(error => console.error("Error deleting message:", error));
        }