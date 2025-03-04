package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Receipt;
import com.example.demo.entity.User;
import com.example.demo.repository.ReceiptRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class OrderController {

	@Autowired
	private ReceiptRepository receiptRepository;

	@GetMapping("/orderHistory")
	public String viewOrderHistory(@RequestParam(value = "receiptID", required = false) Long receiptID, Model model,
			HttpSession session) {
		User user = (User) session.getAttribute("user");

		if (user == null) {
			return "redirect:/login"; // Redirect to login if not logged in
		}

		// ✅ Fetch all completed orders of the user
		List<Receipt> receipts = receiptRepository.findByBuyer(user);

		model.addAttribute("receipts", receipts);
		model.addAttribute("highlightReceiptID", receiptID); // ✅ Pass receiptID for scrolling

		return "orderHistory"; // ✅ Ensure the template name matches the Thymeleaf file
	}
}
