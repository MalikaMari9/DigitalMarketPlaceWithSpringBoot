package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Address;
import com.example.demo.entity.Delivery;
import com.example.demo.entity.Receipt;
import com.example.demo.entity.User;
import com.example.demo.repository.DeliveryRepository;
import com.example.demo.repository.ReceiptRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class DeliHistoryController {

	@Autowired
	private ReceiptRepository receiptRepository;

	@Autowired
	private DeliveryRepository deliveryRepository;

	@GetMapping("/deliHistory")
	public String getDeliHistory(Model model, HttpSession session, // ✅ Use session to get the logged-in user
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "createdAt") String sortBy) {
		int pageSize = 10; // Number of receipts per page

		// ✅ Retrieve the logged-in user from the session
		User seller = (User) session.getAttribute("user");

		if (seller == null) {
			return "redirect:/login"; // Redirect to login if session is missing
		}

		// ✅ Fetch only receipts where the logged-in user is the seller
		PageRequest pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, sortBy));
		Page<Receipt> salesPage = receiptRepository.findBySeller(seller, pageable);

		model.addAttribute("salesReceipts", salesPage.getContent());
		model.addAttribute("salesPage", salesPage);
		model.addAttribute("selectedSortBy", sortBy);

		return "delihistory"; // ✅ Matches the Thymeleaf template name
	}

	@GetMapping("/deli-track")
	public String trackDeli(@RequestParam(value = "receiptID", required = false) Long receiptID, Model model,
			HttpSession session) {

		if (receiptID == null) {
			model.addAttribute("error", "No receipt ID provided.");
			return "deliverytrack";
		}

		Object userObj = session.getAttribute("user");
		if (userObj == null) {
			return "redirect:/login";
		}

		Receipt receipt = receiptRepository.findById(receiptID).orElse(null);
		if (receipt == null) {
			model.addAttribute("error", "Delivery record not found.");
			return "deliverytrack";
		}

		Delivery delivery = receipt.getDelivery();
		if (delivery == null) {
			model.addAttribute("error", "Delivery details not available.");
			return "deliverytrack";
		}

		Address address = delivery.getAddress();
		if (address == null) {
			model.addAttribute("error", "Address details missing.");
			return "deliverytrack";
		}

		// ✅ Calculate days difference between updatedAt and today
		long daysDifference = 0;
		if (delivery.getUpdatedAt() != null) {
			daysDifference = java.time.temporal.ChronoUnit.DAYS.between(delivery.getUpdatedAt(),
					java.time.LocalDateTime.now());
		}

		// ✅ Get delivery status
		String deliveryStatus = delivery.getStatus().name();

		// ✅ Pass the objects, status, and daysDifference to Thymeleaf
		model.addAttribute("receipt", receipt);
		model.addAttribute("delivery", delivery);
		model.addAttribute("address", address);
		model.addAttribute("daysDifference", daysDifference);
		model.addAttribute("deliveryStatus", deliveryStatus);

		return "deliverytrack";
	}

	@GetMapping("/deliBuyer")
	public String getDeliHistory(Model model, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "") String search, @RequestParam(defaultValue = "createdAt") String sortBy,
			HttpSession session) {
		// ✅ Get logged-in user
		Object userObj = session.getAttribute("user");
		if (userObj == null) {
			return "redirect:/login"; // Redirect to login if not logged in
		}
		User buyer = (User) userObj;

		int pageSize = 10; // Number of receipts per page
		PageRequest pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, sortBy));

		// ✅ Fetch sales history only for the logged-in buyer
		Page<Receipt> salesPage = receiptRepository.findByBuyer(buyer, pageable);

		model.addAttribute("salesReceipts", salesPage.getContent());
		model.addAttribute("salesPage", salesPage);
		model.addAttribute("searchQuery", search);
		model.addAttribute("selectedSortBy", sortBy);

		return "delihistoryBuyer"; // Matches Thymeleaf template name
	}

	@GetMapping("/deli-track-buyer")
	public String trackDeliBuyer(@RequestParam(value = "receiptID", required = false) Long receiptID, Model model,
			HttpSession session) {

		if (receiptID == null) {
			model.addAttribute("error", "No receipt ID provided.");
			return "deliverytrack";
		}

		Object userObj = session.getAttribute("user");
		if (userObj == null) {
			return "redirect:/login";
		}

		Receipt receipt = receiptRepository.findById(receiptID).orElse(null);
		if (receipt == null) {
			model.addAttribute("error", "Delivery record not found.");
			return "deliverytrack";
		}

		Delivery delivery = receipt.getDelivery();
		if (delivery == null) {
			model.addAttribute("error", "Delivery details not available.");
			return "deliverytrack";
		}

		Address address = delivery.getAddress();
		if (address == null) {
			model.addAttribute("error", "Address details missing.");
			return "deliverytrack";
		}

		// ✅ Calculate days difference between updatedAt and today
		long daysDifference = 0;
		if (delivery.getUpdatedAt() != null) {
			daysDifference = java.time.temporal.ChronoUnit.DAYS.between(delivery.getUpdatedAt(),
					java.time.LocalDateTime.now());
		}

		// ✅ Get delivery status
		String deliveryStatus = delivery.getStatus().name();

		// ✅ Pass the objects, status, and daysDifference to Thymeleaf
		model.addAttribute("receipt", receipt);
		model.addAttribute("delivery", delivery);
		model.addAttribute("address", address);
		model.addAttribute("daysDifference", daysDifference);
		model.addAttribute("deliveryStatus", deliveryStatus);

		return "deliTrackBuyer";
	}

	@PostMapping("/receiveOrder")
	public String receiveOrder(@RequestParam Long receiptID, HttpSession session) {
		// ✅ Ensure user is logged in
		Object userObj = session.getAttribute("user");
		if (userObj == null) {
			return "redirect:/login"; // Redirect if not logged in
		}

		// ✅ Find the receipt
		Optional<Receipt> receiptOpt = receiptRepository.findById(receiptID);
		if (receiptOpt.isEmpty()) {
			return "redirect:/deli-track?receiptID=" + receiptID + "&error=Receipt not found";
		}

		Receipt receipt = receiptOpt.get();
		Delivery delivery = receipt.getDelivery();

		// ✅ Update status only if it's DELIVERED
		if (delivery.getStatus() == Delivery.DeliveryStatus.DELIVERED) {
			delivery.setStatus(Delivery.DeliveryStatus.RECEIVED);
			delivery.setUpdatedAt(LocalDateTime.now()); // ✅ Update timestamp
			deliveryRepository.save(delivery);
		}

		return "redirect:/deli-track?receiptID=" + receiptID;
	}

}
