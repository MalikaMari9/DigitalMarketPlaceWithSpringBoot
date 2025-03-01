package com.example.demo.controller.chat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Conversation;
import com.example.demo.entity.Message;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/messages")
public class ChatController {

	private final MessageRepository messageRepository;
	private final UserRepository userRepository;
	private final ConversationRepository conversationRepository;
	private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

	public ChatController(MessageRepository messageRepository, ConversationRepository conversationRepository,
			UserRepository userRepository) {
		this.messageRepository = messageRepository;
		this.userRepository = userRepository;
		this.conversationRepository = conversationRepository;
	}

	// 📌 Send Text Message
	@PostMapping
	public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> requestData) {
		try {
			int senderID = Integer.parseInt(requestData.get("senderID").toString());
			int receiverID = Integer.parseInt(requestData.get("receiverID").toString());
			String messageText = requestData.get("message").toString();

			// Check if conversation exists
			List<Conversation> existingConversations = conversationRepository.findBySenderIDOrReceiverID(senderID,
					senderID);

			Conversation conversation;
			boolean conversationExists = existingConversations.stream()
					.anyMatch(conv -> (conv.getSenderID() == senderID && conv.getReceiverID() == receiverID)
							|| (conv.getSenderID() == receiverID && conv.getReceiverID() == senderID));

			if (!conversationExists) {
				conversation = new Conversation(senderID, receiverID);
			} else {
				conversation = existingConversations.stream()
						.filter(conv -> (conv.getSenderID() == senderID && conv.getReceiverID() == receiverID)
								|| (conv.getSenderID() == receiverID && conv.getReceiverID() == senderID))
						.findFirst().get();
			}

			conversation.setUpdatedAt(java.time.LocalDateTime.now()); // Update the timestamp
			conversationRepository.save(conversation);

			Message message = new Message(senderID, receiverID, messageText, null);
			messageRepository.save(message);

			return ResponseEntity.ok(Map.of("success", true, "message", "Message sent successfully"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body(Map.of("error", "Failed to send message"));
		}
	}

	private void createUploadDirIfNotExists() {
		File directory = new File(UPLOAD_DIR);
		if (!directory.exists()) {
			boolean created = directory.mkdirs();
			if (created) {
				System.out.println("✅ Upload directory created: " + UPLOAD_DIR);
			} else {
				System.err.println("❌ Failed to create upload directory: " + UPLOAD_DIR);
			}
		}
	}

	@GetMapping("/conversations")
	@ResponseBody
	public ResponseEntity<?> getConversations(HttpSession session) {
		String loggedInUsername = (String) session.getAttribute("username");
		System.out.println("Session ID: " + session.getId());
		System.out.println("Session username: " + loggedInUsername);

		if (loggedInUsername == null) {
			return ResponseEntity.status(401).body(Map.of("error", "User not logged in."));
		}
		Long senderIDLong = (Long) session.getAttribute("user_id");
		int senderID = senderIDLong.intValue();

		List<Conversation> conversations = conversationRepository.findBySenderIDOrReceiverID(senderID, senderID);

		List<Map<String, Object>> conversationUsers = conversations.stream()
				.sorted((c1, c2) -> c2.getUpdatedAt().compareTo(c1.getUpdatedAt())) // Sort by updatedAt in descending
																					// order
				.map(conversation -> {
					// Get the other user's ID (not the current user)
					int otherUserId = conversation.getSenderID() == senderID ? conversation.getReceiverID()
							: conversation.getSenderID();
					Long otherUserIdLong = Long.valueOf(otherUserId);
					return userRepository.findById(otherUserIdLong).map(user -> {
						Map<String, Object> userMap = new HashMap<>();
						userMap.put("user_id", user.getUserID());
						userMap.put("username", user.getUsername());
						userMap.put("email", user.getEmail());
						userMap.put("profile", user.getProfilePath());
						userMap.put("conversation_id", conversation.getConversationID());

						return userMap;
					}).orElse(null);
				}).filter(userMap -> userMap != null).collect(Collectors.toList());

		return ResponseEntity.ok(conversationUsers);
	}

	@GetMapping("/search/users")
	@ResponseBody
	public ResponseEntity<?> getUsers(HttpSession session, @RequestParam("username") String username) {
		String loggedInUsername = (String) session.getAttribute("username");
		System.out.println("Session ID: " + session.getId());
		System.out.println("Session username: " + loggedInUsername);

		if (loggedInUsername == null) {
			return ResponseEntity.status(401).body(Map.of("error", "User not logged in."));
		}

		List<Map<String, Object>> users = userRepository.findByUsernameContainingIgnoreCase(username).stream()
				.filter(user -> !user.getUsername().equals(loggedInUsername)).map(user -> {
					Map<String, Object> userMap = new HashMap<>();
					// Use a consistent method name (getUserID) to access the user id.
					userMap.put("user_id", user.getUserID());
					userMap.put("username", user.getUsername());
					userMap.put("email", user.getEmail());
					userMap.put("profile", user.getProfilePath());
					return userMap;
				}).collect(Collectors.toList());

		return ResponseEntity.ok(users);
	}

	// 📌 Upload Image Message
	@PostMapping("/upload")
	public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file,
			@RequestParam("senderID") int senderID, @RequestParam("receiverID") int receiverID) {
		try {
			createUploadDirIfNotExists(); // ✅ Ensure directory exists

			if (file.isEmpty()) {
				return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
			}

			// ✅ Generate unique filename
			String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
			File uploadFile = new File(UPLOAD_DIR, fileName);

			// ✅ Save file to `uploads/` directory
			file.transferTo(uploadFile);

			// ✅ Save message with image URL (relative path)
			Message message = new Message(senderID, receiverID, null, fileName);
			messageRepository.save(message);

			return ResponseEntity.ok(Map.of("success", true, "image_url", fileName));
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body(Map.of("error", "Failed to upload image"));
		}
	}

	// 📌 Fetch Messages
	@GetMapping
	public ResponseEntity<?> getMessages(@RequestParam int senderID, @RequestParam int receiverID) {
		try {
			List<Message> messages = messageRepository
					.findBySenderIDAndReceiverIDOrSenderIDAndReceiverIDOrderByCreatedAtAsc(senderID, receiverID,
							receiverID, senderID);

			return ResponseEntity.ok(messages);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body(Map.of("error", "Failed to retrieve messages"));
		}
	}

	// 📌 Edit Message
	@PutMapping
	public ResponseEntity<?> editMessage(@RequestBody Map<String, Object> requestData) {
		try {
			long messageID = Long.parseLong(requestData.get("messageID").toString());
			String updatedMessage = requestData.get("updatedMessage").toString();

			Message message = messageRepository.findById(messageID).orElse(null);
			if (message == null) {
				return ResponseEntity.status(404).body(Map.of("error", "Message not found"));
			}

			message.setMessage(updatedMessage);
			messageRepository.save(message);

			return ResponseEntity.ok(Map.of("success", true, "message", "Message updated successfully"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body(Map.of("error", "Failed to update message"));
		}
	}

	// 📌 Delete Message
	@DeleteMapping
	public ResponseEntity<?> deleteMessage(@RequestParam long messageID) {
		try {
			if (!messageRepository.existsById(messageID)) {
				return ResponseEntity.status(404).body(Map.of("error", "Message not found"));
			}

			messageRepository.deleteById(messageID);
			return ResponseEntity.ok(Map.of("success", true, "message", "Message deleted successfully"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body(Map.of("error", "Failed to delete message"));
		}
	}
}
