package com.example.demo.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

	@PostMapping("/loginPage")
	public String login(@RequestParam String username, HttpSession session) {
		// Fetch user from database by username
		Optional<User> optionalUser = userRepository.findByUsername(username);

		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			session.setAttribute("user", user); // Store user in session
			System.out.print("User's role is" + user.getRole());
			System.out.print("Session ID is" + session.getId());
			// Redirect based on role
			return "redirect:/home";
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
}
