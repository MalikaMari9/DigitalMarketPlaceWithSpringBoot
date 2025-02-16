package com.example.demo.controller;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller

public class UserController {

	@Autowired
	private UserRepository userRepository;

	// Show Registration Form
	@GetMapping("/register")
	public String showRegistrationForm(Model model) {
		model.addAttribute("user", new User());
		return "Registeration";
	}

	// Handle Registration
	@PostMapping("/register")
	public String registerUser(@ModelAttribute User user, Model model) throws NoSuchAlgorithmException {
		// Check if username exists
		if (userRepository.findByUsername(user.getUsername()).isPresent()) {
			model.addAttribute("usernameError", "Username is already taken");
			return "Registeration";
		}

		// Hash password using SHA-256
		user.setPassword(hashPassword(user.getPassword()));

		// Save user
		userRepository.save(user);

		return "redirect:login";
	}

	// Show Login Form
	@GetMapping("/login")
	public String showLoginForm() {
		return "login";
	}

	@PostMapping("/login")
	public String loginUser(@RequestParam String username, @RequestParam String password, HttpServletRequest request,
			Model model) throws NoSuchAlgorithmException {
		if (username.equals("ADMIN")) {
			return "redirect:/admin/viewDashboard";
		}
		Optional<User> userOptional = userRepository.findByUsername(username);

		if (userOptional.isPresent()) {
			User user = userOptional.get();

			// Compare stored hash with newly hashed input password
			if (user.getPassword().equals(hashPassword(password))) {
				HttpSession session = request.getSession();
				session.setAttribute("user", user);
				return "redirect:/home";
			}
		}

		model.addAttribute("loginError", "Invalid username or password");
		return "login";
	}

	// Notification

	@Autowired
	private NotificationRepository notificationRepo;

	// ✅ Get all notifications for logged-in user
	@GetMapping("/notifications")
	public String viewNotifications(HttpSession session, Model model) {
		User user = (User) session.getAttribute("user");

		if (user == null) {
			return "redirect:/loginPage"; // ✅ Redirect if user is not logged in
		}

		// ✅ Fetch all notifications for the user
		List<Notification> notifications = notificationRepo.findByUserOrderByCreatedAtDesc(user);
		model.addAttribute("notifications", notifications);

		// ✅ Mark all notifications as seen
		notifications.forEach(noti -> noti.setSeen(true));
		notificationRepo.saveAll(notifications);

		return "notification"; // ✅ Thymeleaf file
	}

	// ✅ Get only unread notifications for logged-in user
	@GetMapping("/notifications/unread")
	@ResponseBody
	public ResponseEntity<List<Notification>> getUnreadNotifications(HttpSession session) {
		User user = (User) session.getAttribute("user");

		if (user == null) {
			return ResponseEntity.badRequest().build();
		}

		List<Notification> unreadNotifications = notificationRepo.findByUserAndSeenFalseOrderByCreatedAtDesc(user);
		return ResponseEntity.ok(unreadNotifications);
	}

	// Method to hash password using SHA-256
	private String hashPassword(String password) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] hashedBytes = md.digest(password.getBytes());

		// Convert bytes to hex format
		StringBuilder hexString = new StringBuilder();
		for (byte b : hashedBytes) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}
}
