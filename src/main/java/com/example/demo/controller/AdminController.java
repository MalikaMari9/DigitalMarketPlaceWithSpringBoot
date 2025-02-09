package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {
	@GetMapping("/admin/viewDashboard")
	public String viewDashboard() {
		return "admin/dashboard";

	}

	@GetMapping("/admin/approvals")
	public String viewApprovals() {
		return "admin/approvals";

	}

	@GetMapping("/admin/auctions")
	public String viewAuctions() {
		return "admin/auctions";

	}

	@GetMapping("/admin/delivery")
	public String viewDelivery() {
		return "admin/delivery";

	}

	@GetMapping("/admin/items")
	public String viewItems() {
		return "admin/items";

	}

	@GetMapping("/admin/orders")
	public String viewOrders() {
		return "admin/orders";

	}

	@GetMapping("/admin/sellers")
	public String viewSellers() {
		return "admin/sellers";

	}

	@GetMapping("/admin/users")
	public String viewUsers() {
		return "admin/users";

	}

}
