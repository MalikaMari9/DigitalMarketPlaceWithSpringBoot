package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class TBDLoginController {

	@Autowired
	private UserRepository userRepository;

	@GetMapping("/loginPage")
	public String goLogin() {
		return "TBDLogin";
	}

	@GetMapping("/sellerDashboard")
	public String sellerLogin(HttpSession session) {
		User seller = (User) session.getAttribute("user");
		if (seller == null) {
			return "redirect:/loginPage";
		}
		return "sellerDashboard";
	}

	@PostMapping("/loginPage")
	public String login(@RequestParam String username, HttpSession session) {
		if (username.equals("ADMIN")) {
			session.setAttribute("admin", true);
			return "redirect:/admin/viewDashboard";
		}

		// Fetch user from database by username
		Optional<User> optionalUser = userRepository.findByUsername(username);

		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			session.setAttribute("user", user); // Store user in session

			session.setAttribute("username", user.getUsername());
			session.setAttribute("user_id", user.getUserID());
			System.out.println("User's role is " + user.getRole());
			System.out.println("Session ID is " + session.getId());

			// Redirect based on role
			if ("BUYER".equals(user.getRole())) { // Corrected string comparison
				return "redirect:/home";
			} else {
				return "redirect:/sellerDashboard";
			}
		} else {
			// Redirect back to login with an error
			return "redirect:/loginPage?error=User not found";
		}
	}

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate(); // Clear session
		return "redirect:/home";
	}

	// API endpoint to fetch users (for your fetch call)
	@GetMapping("/api/users")
	@ResponseBody
	public ResponseEntity<?> getUsers(HttpSession session) {
		String loggedInUsername = (String) session.getAttribute("username");
		System.out.println("Session ID: " + session.getId());
		System.out.println("Session username: " + loggedInUsername);

		if (loggedInUsername == null) {
			return ResponseEntity.status(401).body(Map.of("error", "User not logged in."));
		}

		List<Map<String, Object>> users = userRepository.findAll().stream()
				.filter(user -> !user.getUsername().equals(loggedInUsername)).map(user -> {
					Map<String, Object> userMap = new HashMap<>();
					userMap.put("user_id", user.getUserID());
					userMap.put("username", user.getUsername());
					userMap.put("email", user.getEmail());
					String profile = user.getProfilePath();
					if (profile == null || profile.isEmpty()) {
						profile = "profiledefault.png"; // Adjust this default file name/path as needed.
					}
					userMap.put("profile", profile);
					return userMap;
				}).collect(Collectors.toList());

		return ResponseEntity.ok(users);
	}

	// API endpoint for authentication login (for fetch calls)
	@PostMapping("/api/auth/login")
	@ResponseBody
	public ResponseEntity<?> apiLogin(@RequestBody Map<String, String> loginData, HttpSession session) {
		// Ensure loginData contains the username and password
		String username = loginData.get("username");
		String password = loginData.get("password");

		if (username == null || password == null) {
			return ResponseEntity.badRequest().body(Map.of("error", "Username or password cannot be null"));
		}

		// Find user by username
		Optional<User> optionalUser = userRepository.findByUsername(username);

		// Validate user and password
		if (optionalUser.isPresent()) {
			User foundUser = optionalUser.get();

			// Ensure that the found user has a valid password
			if (foundUser.getPassword() != null && foundUser.getPassword().equals(password)) {
				session.setAttribute("username", foundUser.getUsername());
				session.setAttribute("user_id", foundUser.getUserID());
				String profile = foundUser.getProfilePath();
				if (profile == null || profile.isEmpty()) {
					profile = "profiledefault.png";
				}

				return ResponseEntity.ok(Map.of("message", "Login successful", "username", foundUser.getUsername(),
						"user_id", foundUser.getUserID(), "profile", profile, "role", foundUser.getRole()));
			} else {
				return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
			}
		} else {
			return ResponseEntity.status(401).body(Map.of("error", "User not found"));
		}
	}
}
