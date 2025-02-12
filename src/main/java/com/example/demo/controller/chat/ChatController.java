package com.example.demo.controller.chat;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Message;
import com.example.demo.entity.User;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/messages")
public class ChatController {

	private final MessageRepository messageRepository;
	private final UserRepository userRepository;

	public ChatController(MessageRepository messageRepository, UserRepository userRepository) {
		this.messageRepository = messageRepository;
		this.userRepository = userRepository;
	}

	// ✅ Send Message (Ensures User is Logged In)
	@PostMapping
	public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> requestData, HttpSession session) {
		Long senderID = (Long) session.getAttribute("user");
		if (senderID == null) {
			return ResponseEntity.status(401).body(Map.of("error", "User not logged in"));
		}

		Long receiverID = Long.parseLong(requestData.get("receiverID").toString());
		String messageText = requestData.get("message").toString();

		Optional<User> sender = userRepository.findById(senderID);
		Optional<User> receiver = userRepository.findById(receiverID);

		if (sender.isEmpty() || receiver.isEmpty()) {
			return ResponseEntity.status(404).body(Map.of("error", "User not found"));
		}

		Message message = new Message(sender.get(), receiver.get(), messageText, null);
		messageRepository.save(message);

		return ResponseEntity.ok(Map.of("success", true, "message", "Message sent successfully"));
	}

	// ✅ Fetch Messages Between Sender & Receiver
	@GetMapping
	public ResponseEntity<?> getMessages(@RequestParam Long receiverID, HttpSession session) {
		Long senderID = (Long) session.getAttribute("user");
		if (senderID == null) {
			return ResponseEntity.status(401).body(Map.of("error", "User not logged in"));
		}

		Optional<User> sender = userRepository.findById(senderID);
		Optional<User> receiver = userRepository.findById(receiverID);

		if (sender.isEmpty() || receiver.isEmpty()) {
			return ResponseEntity.status(404).body(Map.of("error", "User not found"));
		}

		List<Message> messages = messageRepository.findBySenderAndReceiverOrSenderAndReceiverOrderByCreatedAtAsc(
				sender.get(), receiver.get(), receiver.get(), sender.get());

		return ResponseEntity.ok(messages);
	}
}
