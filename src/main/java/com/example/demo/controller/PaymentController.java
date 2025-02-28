
package com.example.demo.controller;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.Address;
import com.example.demo.entity.Cart;
import com.example.demo.entity.Delivery;
import com.example.demo.entity.Order;
import com.example.demo.entity.Payment;
import com.example.demo.entity.Receipt;
import com.example.demo.entity.User;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.DeliveryRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.ReceiptRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class PaymentController {

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

	@GetMapping("/checkout")
	public String checkoutPage(@RequestParam("sellerID") Long sellerID,
			@RequestParam(value = "deliveryFee", required = false, defaultValue = "5.00") double deliveryFee,
			ModelMap model, HttpSession session) {
		User user = (User) session.getAttribute("user");

		if (user == null) {
			return "redirect:/loginPage"; // Redirect to login if not logged in
		}

		// Fetch all cart items for the logged-in user
		List<Cart> cartItems = cartRepository.findByUser(user);

		if (cartItems == null || cartItems.isEmpty()) {
			model.addAttribute("emptyCartMessage", "Your cart is empty.");
			model.addAttribute("cartCount", 0);
			return "proceedCheckout"; // Redirect to empty cart page
		}

		// ✅ Filter only items from the selected seller
		List<Cart> selectedCartItems = cartItems.stream().filter(cart -> cart.getItem() != null
				&& cart.getItem().getSeller() != null && cart.getItem().getSeller().getUserID().equals(sellerID))
				.collect(Collectors.toList());

		if (selectedCartItems.isEmpty()) {
			model.addAttribute("emptyCartMessage", "No items found for this seller.");
			model.addAttribute("cartCount", 0);
			return "proceedCheckout";
		}

		// ✅ Get Seller Info from Cart Items
		User seller = selectedCartItems.get(0).getItem().getSeller();
		model.addAttribute("seller", seller); // ✅ Pass seller to the view

		// ✅ Calculate subtotal
		double subtotal = selectedCartItems.stream().mapToDouble(cart -> cart.getItem().getPrice() * cart.getQuantity())
				.sum();
		// double deliveryFee = 5.00;

		model.addAttribute("cartItems", selectedCartItems);
		model.addAttribute("subtotal", subtotal);
		model.addAttribute("cartCount", cartItems.size());
		model.addAttribute("deliveryFee", deliveryFee);

		return "payment"; // Ensure this Thymeleaf file exists
	}

	@PostMapping("/place-order")
	@ResponseBody
	public Map<String, Object> placeOrder(@RequestBody Map<String, String> requestData, HttpSession session) {
		Map<String, Object> response = new HashMap<>();

		// ✅ Check if User is Logged In
		User buyer = (User) session.getAttribute("user");
		if (buyer == null) {
			response.put("success", false);
			response.put("message", "User not logged in.");
			return response;
		}

		// ✅ Retrieve sellerID from request
		Long sellerID;
		try {
			sellerID = Long.parseLong(requestData.get("sellerID"));
		} catch (NumberFormatException e) {
			response.put("success", false);
			response.put("message", "Invalid seller ID: " + requestData.get("sellerID"));
			return response;
		}

		// ✅ Validate Payment Method
		String paymentMethod = requestData.get("paymentMethod");
		if (paymentMethod == null || paymentMethod.isEmpty()) {
			response.put("success", false);
			response.put("message", "Payment method is required.");
			return response;
		}

		// ✅ Fetch Address
		Long addressID = 1L;
		Optional<Address> addressOptional = addressRepository.findById(addressID);
		if (!addressOptional.isPresent()) {
			response.put("success", false);
			response.put("message", "Invalid address: Address ID 1 does not exist.");
			return response;
		}
		Address defaultAddress = addressOptional.get();

		// ✅ Fetch and filter cart items for the selected seller only
		List<Cart> cartItems = cartRepository.findByUser(buyer);
		List<Cart> selectedCartItems = cartItems.stream()
				.filter(cart -> cart.getItem().getSeller().getUserID().equals(sellerID)).collect(Collectors.toList());

		if (selectedCartItems.isEmpty()) {
			response.put("success", false);
			response.put("message", "No items found for this seller.");
			return response;
		}

		// ✅ Get Delivery Fee
		double deliveryFee;
		try {
			deliveryFee = Double.parseDouble(requestData.getOrDefault("deliveryFee", "5.00"));
		} catch (NumberFormatException e) {
			response.put("success", false);
			response.put("message", "Invalid delivery fee.");
			return response;
		}

		// ✅ Calculate total price
		double subtotal = selectedCartItems.stream().mapToDouble(cart -> cart.getItem().getPrice() * cart.getQuantity())
				.sum();
		double totalPrice = subtotal + deliveryFee;

		// ✅ Create & Save Receipt
		Receipt receipt = new Receipt();
		receipt.setBuyer(buyer);
		receipt.setSeller(selectedCartItems.get(0).getItem().getSeller()); // ✅ Get seller from selected items
		receipt.setTotalPrice(totalPrice);
		receipt.setDeliFee(deliveryFee);
		receipt = receiptRepository.save(receipt);

		// ✅ Create & Save Orders
		for (Cart cart : selectedCartItems) {
			Order order = new Order();
			order.setReceipt(receipt);
			order.setBuyer(buyer);
			order.setSeller(cart.getItem().getSeller());
			order.setItem(cart.getItem());
			order.setQuantity(cart.getQuantity());
			order.setPrice(cart.getItem().getPrice());
			orderRepository.save(order);
		}

		// ✅ Create & Save Delivery
		Delivery delivery = new Delivery();
		delivery.setReceipt(receipt);
		delivery.setStatus(Delivery.DeliveryStatus.PENDING);
		delivery.setAddress(defaultAddress);
		deliveryRepository.save(delivery);

		// ✅ Create & Save Payment
		Payment payment = new Payment();
		payment.setReceipt(receipt);
		payment.setUser(buyer);
		payment.setAmount(BigDecimal.valueOf(totalPrice));
		payment.setPaymentMethod(paymentMethod);
		payment.setPaymentStatus("PENDING");
		paymentRepository.save(payment);

		// ✅ Remove ordered items from cart
		cartRepository.deleteAll(selectedCartItems);

		response.put("success", true);
		response.put("message", "Order placed successfully!");
		return response;
	}

}