package com.example.demo.controller.chat;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Message;
import com.example.demo.repository.MessageRepository;

@RestController
@RequestMapping("/api/messages")
public class ChatController {

	private final MessageRepository messageRepository;
	private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

	public ChatController(MessageRepository messageRepository) {
		this.messageRepository = messageRepository;
	}

	// 📌 Send Text Message
	@PostMapping
	public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> requestData) {
		try {
			int senderID = Integer.parseInt(requestData.get("senderID").toString());
			int receiverID = Integer.parseInt(requestData.get("receiverID").toString());
			String messageText = requestData.get("message").toString();

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
