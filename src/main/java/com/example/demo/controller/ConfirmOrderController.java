package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.DeliTrack;
import com.example.demo.entity.Delivery;
import com.example.demo.entity.Notification;
import com.example.demo.entity.Receipt;
import com.example.demo.entity.User;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.DeliTrackRepository;
import com.example.demo.repository.DeliveryRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.ReceiptRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class ConfirmOrderController {

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private ReceiptRepository receiptRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private DeliveryRepository deliveryRepository;

	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private AddressRepository addressRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private DeliTrackRepository deliTrackRepository; // ✅ Added repository for tracking deliveries

	@GetMapping("/confirmDelivery")
	public String confirmDeliveryPage(ModelMap model, HttpSession session) {
		User seller = (User) session.getAttribute("user");
		if (seller == null) {
			return "redirect:/loginPage";
		}

		List<Receipt> pendingReceipts = receiptRepository.findBySeller(seller).stream()
				.filter(receipt -> receipt.getDelivery().getStatus() == Delivery.DeliveryStatus.PENDING)
				.collect(Collectors.toList());

		model.addAttribute("pendingReceipts", pendingReceipts);
		return "confirmDelivery";
	}

	@PostMapping("/confirmDelivery")
	@ResponseBody
	public Map<String, Object> confirmDelivery(@RequestBody Map<String, Long> requestData, HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		User seller = (User) session.getAttribute("user");

		if (seller == null) {
			response.put("success", false);
			response.put("message", "User not logged in.");
			return response;
		}

		Long receiptID = requestData.get("receiptID");
		Optional<Receipt> optionalReceipt = receiptRepository.findById(receiptID);

		if (optionalReceipt.isEmpty()) {
			response.put("success", false);
			response.put("message", "Receipt not found.");
			return response;
		}

		Receipt receipt = optionalReceipt.get();

		if (!receipt.getSeller().getUserID().equals(seller.getUserID())) {
			response.put("success", false);
			response.put("message", "Unauthorized action.");
			return response;
		}

		Delivery delivery = receipt.getDelivery();
		delivery.setStatus(Delivery.DeliveryStatus.DELIVERED);
		delivery.setUpdatedAt(LocalDateTime.now());
		deliveryRepository.save(delivery);

		// ✅ Insert tracking entry in deli_track_tbl
		DeliTrack track = new DeliTrack();
		track.setDelivery(delivery);
		track.setStatus(Delivery.DeliveryStatus.DELIVERED);
		track.setNote("Order has been delivered.");
		deliTrackRepository.save(track);

		// ✅ Notify Buyer
		Notification notification = new Notification();
		notification.setUser(receipt.getBuyer());
		notification.setNotiText("Your order with Receipt ID #" + receiptID + " has been delivered.");
		notification.setNotiType("ORDER_DELIVERED");
		notification.setReceipt(receipt);
		notificationRepository.save(notification);

		response.put("success", true);
		response.put("message", "Delivery confirmed successfully.");
		return response;
	}

	@PostMapping("/cancelDelivery")
	@ResponseBody
	public Map<String, Object> cancelDelivery(@RequestBody Map<String, Long> requestData, HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		User seller = (User) session.getAttribute("user");

		if (seller == null) {
			response.put("success", false);
			response.put("message", "User not logged in.");
			return response;
		}

		Long receiptID = requestData.get("receiptID");
		Optional<Receipt> optionalReceipt = receiptRepository.findById(receiptID);

		if (optionalReceipt.isEmpty()) {
			response.put("success", false);
			response.put("message", "Receipt not found.");
			return response;
		}

		Receipt receipt = optionalReceipt.get();

		if (!receipt.getSeller().getUserID().equals(seller.getUserID())) {
			response.put("success", false);
			response.put("message", "Unauthorized action.");
			return response;
		}

		Delivery delivery = receipt.getDelivery();
		delivery.setStatus(Delivery.DeliveryStatus.CANCELED);
		delivery.setUpdatedAt(LocalDateTime.now());
		deliveryRepository.save(delivery);

		// ✅ Insert tracking entry in deli_track_tbl
		DeliTrack track = new DeliTrack();
		track.setDelivery(delivery);
		track.setStatus(Delivery.DeliveryStatus.CANCELED);
		track.setNote("Order has been canceled.");
		deliTrackRepository.save(track);

		// ✅ Notify Buyer
		Notification notification = new Notification();
		notification.setUser(receipt.getBuyer());
		notification.setNotiText("Your order with Receipt ID #" + receipt.getReceiptID() + " has been canceled.");
		notification.setNotiType("ORDER_CANCELED");
		notification.setReceipt(receipt);
		notificationRepository.save(notification);

		response.put("success", true);
		response.put("message", "Delivery canceled successfully.");
		return response;
	}
}
