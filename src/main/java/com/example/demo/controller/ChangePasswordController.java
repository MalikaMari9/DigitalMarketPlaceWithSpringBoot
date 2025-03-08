package com.example.demo.controller;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/change-password")
public class ChangePasswordController {

	@Autowired
	private UserRepository userRepository;

	@PostMapping
	@ResponseBody
	public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> requestData,
			HttpSession session) {
		Map<String, Object> response = new HashMap<>();

		// Retrieve the User object from the session
		Object sessionUser = session.getAttribute("user");
		if (!(sessionUser instanceof User)) {
			response.put("success", false);
			response.put("message", "Session expired. Please log in again.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		User user = (User) sessionUser; // Cast session object to User
		Long userId = user.getUserID(); // Extract user ID

		String oldPassword = requestData.get("oldPassword");
		String newPassword = requestData.get("newPassword");

		Optional<User> userOptional = userRepository.findById(userId);
		if (userOptional.isEmpty()) {
			response.put("success", false);
			response.put("message", "User not found.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		User storedUser = userOptional.get();

		// Compare hashed old password with stored password
		if (!storedUser.getPassword().equals(hashPassword(oldPassword))) {
			response.put("success", false);
			response.put("message", "Old password is incorrect.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		// Hash the new password before saving
		storedUser.setPassword(hashPassword(newPassword));
		userRepository.save(storedUser);

		session.invalidate(); // Logout after password change

		response.put("success", true);
		return ResponseEntity.ok(response);
	}

	// Hashing method inside the controller
	private String hashPassword(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashedBytes = md.digest(password.getBytes());

			// Convert bytes to hex format
			StringBuilder hexString = new StringBuilder();
			for (byte b : hashedBytes) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Error hashing password", e);
		}
	}
}
