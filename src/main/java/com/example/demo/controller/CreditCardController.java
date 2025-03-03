package com.example.demo.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.Address;
import com.example.demo.entity.Cart;
import com.example.demo.entity.CreditCard;
import com.example.demo.entity.Delivery;
import com.example.demo.entity.Item;
import com.example.demo.entity.Order;
import com.example.demo.entity.Payment;
import com.example.demo.entity.Receipt;
import com.example.demo.entity.User;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.CreditCardRepository;
import com.example.demo.repository.DeliveryRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.ReceiptRepository;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
public class CreditCardController {

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
	private CreditCardRepository creditCardRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ItemRepository itemRepository;

	@GetMapping("/credit-card-payment")
	public String showCreditCardPaymentPage(
			@RequestParam(value = "amount", required = false, defaultValue = "0.00") double amount,
			@RequestParam(value = "sellerID", required = false) Long sellerID, // ❗️Make sellerID optional
			HttpSession session, Model model) {

		// ✅ Ensure User is Logged In
		User user = (User) session.getAttribute("user");
		if (user == null) {
			return "redirect:/loginPage"; // Redirect to login page if not logged in
		}

		// ✅ If sellerID is missing, show error message and redirect to checkout
		if (sellerID == null) {
			model.addAttribute("errorMessage", "❌ Required request parameter 'sellerID' is missing.");
			model.addAttribute("sellerID", ""); // Ensure it exists for Thymeleaf
			return "errorPage"; // Redirect to error page with message
		}

		// ✅ Fetch and filter cart items for the selected seller only
		List<Cart> cartItems = cartRepository.findByUser(user);
		List<Cart> selectedCartItems = cartItems.stream()
				.filter(cart -> cart.getItem().getSeller().getUserID().equals(sellerID)).collect(Collectors.toList());

		if (selectedCartItems.isEmpty()) {
			// model.addAttribute("errorMessage", "❌ No items found for this seller.");
			model.addAttribute("sellerID", sellerID);
			return "errorPage"; // Redirect to error page with message
		}

		// ✅ Check if any item has insufficient stock BEFORE showing the credit card
		// page
		for (Cart cart : selectedCartItems) {
			Item item = cart.getItem();
			if (item.getQuality() < cart.getQuantity()) {
				model.addAttribute("errorMessage", "❌ Insufficient stock for item: " + item.getItemName());
				model.addAttribute("sellerID", sellerID);
				return "errorPage"; // Redirect to error page
			}
		}

		// ✅ Fetch Seller from Database
		User seller = userRepository.findById(sellerID).orElse(null);

		// ✅ Pass seller & amount to Thymeleaf
		model.addAttribute("totalAmount", amount);
		model.addAttribute("seller", seller);

		return "creditcard"; // ✅ Show credit card payment page
	}

	@PostMapping("/place-order-card")
	@ResponseBody
	@Transactional // Ensures rollback if any failure occurs
	public Map<String, Object> placeOrderWithCard(@RequestBody Map<String, String> requestData, HttpSession session) {
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

		// ✅ Validate Payment Method (Ensure it's credit card)
		String paymentMethod = requestData.get("paymentMethod");
		if (!"online".equalsIgnoreCase(paymentMethod)) {
			response.put("success", false);
			response.put("message", "Invalid payment method. Only 'online' payments allowed.");
			return response;
		}

		// ✅ Validate Credit Card Information
		String cardOwner = requestData.get("cardOwner");
		String cvCode = requestData.get("cvCode"); // ✅ CV Code replaces Card Type
		String cardNumber = requestData.get("cardNumber");
		String expirationDate = requestData.get("expirationDate");
		String postalCode = requestData.get("postalCode");

		if (cardOwner == null || cvCode == null || cardNumber == null || expirationDate == null || postalCode == null) {
			response.put("success", false);
			response.put("message", "Missing credit card details.");
			return response;
		}

		if (cardNumber.length() < 12) {
			response.put("success", false);
			response.put("message", "Invalid card number.");
			return response;
		}

		// ✅ Extract Last 4 Digits of the Card
		String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);

		// ✅ Fetch Default Address (Modify if users can select their address)
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

		// ✅ Save Credit Card Info (Only Last 4 Digits & CV Code)
		CreditCard creditCard = new CreditCard(buyer, lastFourDigits, cardOwner, expirationDate, postalCode, cvCode);
		creditCardRepository.save(creditCard);

		// ✅ Create & Save Receipt
		Receipt receipt = new Receipt();
		receipt.setBuyer(buyer);
		receipt.setSeller(selectedCartItems.get(0).getItem().getSeller());
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

			Item item = cart.getItem();
			int newStock = item.getQuality() - cart.getQuantity();

			if (newStock < 0) {
				response.put("success", false);
				response.put("message", "Insufficient stock for item: " + item.getItemName());
				return response; // Exit if stock is insufficient
			}

			item.setQuality(newStock);
			itemRepository.save(item); // ✅ Update item
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
		payment.setPaymentStatus("PAID");
		payment.setCreditCard(creditCard);
		paymentRepository.save(payment);

		// ✅ Remove ordered items from cart
		cartRepository.deleteAll(selectedCartItems);

		response.put("success", true);
		response.put("message", "Order placed successfully via credit card!");
		return response;
	}
}
