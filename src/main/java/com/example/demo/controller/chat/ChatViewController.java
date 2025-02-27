package com.example.demo.controller.chat;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatViewController {

	@GetMapping("/chat")
	public String chatPage() {
		return "chat"; // This will return chat.html from templates/
	}
}
