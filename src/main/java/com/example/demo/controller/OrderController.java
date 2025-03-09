package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.Delivery;
import com.example.demo.entity.Payment;
import com.example.demo.entity.Receipt;
import com.example.demo.entity.User;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.ReceiptRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class OrderController {

	@Autowired
	private ReceiptRepository receiptRepository;
	@Autowired
	private PaymentRepository paymentRepository;

	@GetMapping("/orderHistory")
	public String viewOrderHistory(@RequestParam(value = "page", defaultValue = "1") int page, // ✅ Default to page 1
			@RequestParam(value = "size", defaultValue = "5") int size, // ✅ Show 5 orders per page
			@RequestParam(value = "receiptID", required = false) Long receiptID, Model model, HttpSession session) {
		User user = (User) session.getAttribute("user");

		if (user == null) {
			return "redirect:/login"; // Redirect to login if not logged in
		}

		// ✅ Fetch all completed orders of the user
		Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
		Page<Receipt> receiptPage = receiptRepository.findByBuyer(user, pageable);

		// ✅ Fetch payment status for each receipt
		Map<Long, String> paymentStatusMap = new HashMap<>();
		for (Receipt receipt : receiptPage.getContent()) {
			Payment payment = paymentRepository.findByReceipt(receipt);
			paymentStatusMap.put(receipt.getReceiptID(), (payment != null) ? payment.getPaymentStatus() : "UNKNOWN");
		}

		model.addAttribute("receiptPage", receiptPage);
		model.addAttribute("paymentStatusMap", paymentStatusMap); // ✅ Pass payment status to the template
		model.addAttribute("highlightReceiptID", receiptID); // ✅ Pass receiptID for scrolling

		return "orderHistory"; // ✅ Ensure the template name matches the Thymeleaf file
	}

	@GetMapping("/saleHistory")
	public String viewSaleHistory(
			@RequestParam(value = "sortBy", required = false, defaultValue = "date") String sortBy,
			@RequestParam(value = "search", required = false, defaultValue = "") String searchQuery,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size, Model model, HttpSession session) {

		User seller = (User) session.getAttribute("user");
		if (seller == null) {
			return "redirect:/login"; // Redirect if not logged in
		}

		// ✅ Define Sorting Strategy
		Sort sort;
		switch (sortBy) {
		case "item":
			sort = Sort.by("orders.item.itemName").ascending();
			break;
		case "price":
			sort = Sort.by("totalPrice").descending();
			break;
		case "buyer":
			sort = Sort.by("buyer.username").ascending();
			break;
		case "status":
			sort = Sort.by("delivery.status").ascending();
			break;
		case "payment_status":
			sort = Sort.by("payment.paymentStatus").ascending(); // ✅ Added Payment Status Sorting
			break;
		default:
			sort = Sort.by("createdAt").descending(); // Default: Sort by date
			break;
		}

		// ✅ Pageable Request
		Pageable pageable = PageRequest.of(page, size, sort);

		// ✅ Fetch Paginated Sale History
		Page<Receipt> salesPage = receiptRepository.findBySellerAndSearch(seller, searchQuery, pageable);

		Map<Long, String> paymentStatusMap = new HashMap<>();
		for (Receipt receipt : salesPage.getContent()) {
			Payment payment = paymentRepository.findByReceipt(receipt);
			if (payment != null) {
				paymentStatusMap.put(receipt.getReceiptID(), payment.getPaymentStatus());
			} else {
				paymentStatusMap.put(receipt.getReceiptID(), "UNKNOWN");
			}
		}

		// ✅ Pass data to Thymeleaf
		model.addAttribute("salesPage", salesPage);
		model.addAttribute("salesReceipts", salesPage.getContent()); // ✅ Ensure the frontend can iterate correctly
		model.addAttribute("selectedSortBy", sortBy);
		model.addAttribute("searchQuery", searchQuery);
		model.addAttribute("paymentStatusMap", paymentStatusMap);
		return "saleHistory";
	}

	@GetMapping("/confirmDelivery/count")
	@ResponseBody
	public Map<String, Integer> getUndeliveredOrderCount(HttpSession session) {
		User seller = (User) session.getAttribute("user");
		Map<String, Integer> response = new HashMap<>();

		if (seller == null) {
			response.put("count", 0);
			return response;
		}

		int undeliveredCount = (int) receiptRepository.findBySeller(seller).stream()
				.filter(receipt -> receipt.getDelivery().getStatus() == Delivery.DeliveryStatus.PENDING).count();

		response.put("count", undeliveredCount);
		return response;
	}

}
