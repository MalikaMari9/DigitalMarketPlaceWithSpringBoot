
package com.example.demo.controller;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.Address;
import com.example.demo.entity.Cart;
import com.example.demo.entity.DeliTrack;
import com.example.demo.entity.Delivery;
import com.example.demo.entity.Item;
import com.example.demo.entity.Notification;
import com.example.demo.entity.Order;
import com.example.demo.entity.Payment;
import com.example.demo.entity.Receipt;
import com.example.demo.entity.User;
import com.example.demo.entity.Auction.Auction;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.DeliTrackRepository;
import com.example.demo.repository.DeliveryRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.ReceiptRepository;
import com.example.demo.repository.Auction.AuctionRepository;
import com.example.demo.repository.Auction.AuctionTrackRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class CheckoutController {

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
	private DeliTrackRepository deliTrackRepository;

	@Autowired
	private AuctionTrackRepository auctionTrackRepository;
	@Autowired
	private AuctionRepository auctionRepo;

	@GetMapping("/checkout")
	public String checkoutPage(@RequestParam("sellerID") Long sellerID,
			@RequestParam(value = "deliveryFee", required = false, defaultValue = "5.00") double deliveryFee,
			ModelMap model, HttpSession session) {

		User user = (User) session.getAttribute("user");

		if (user == null) {
			return "redirect:/loginPage"; // Redirect if not logged in
		}

		// ✅ Fetch user's saved addresses
		List<Address> userAddresses = addressRepository.findByUser(user);

		// ✅ Fetch all cart items for the user
		List<Cart> cartItems = cartRepository.findByUser(user);

		if (cartItems == null || cartItems.isEmpty()) {
			model.addAttribute("emptyCartMessage", "Your cart is empty.");
			model.addAttribute("cartCount", 0);
			return "cart"; // Redirect to cart page
		}

		// ✅ Filter only items from the selected seller
		List<Cart> selectedCartItems = cartItems.stream().filter(cart -> cart.getItem() != null
				&& cart.getItem().getSeller() != null && cart.getItem().getSeller().getUserID().equals(sellerID))
				.collect(Collectors.toList());

		if (selectedCartItems.isEmpty()) {
			model.addAttribute("emptyCartMessage", "No items found for this seller.");
			model.addAttribute("cartCount", 0);
			return "cart";
		}

		// ✅ Get Seller Info from Cart Items
		User seller = selectedCartItems.get(0).getItem().getSeller();
		model.addAttribute("seller", seller); // ✅ Pass seller to the view

		// ✅ Fetch highest bid prices for auction items
		Map<Long, Double> auctionMaxBids = new HashMap<>();

		// ✅ Get all auction items in a single query
		List<Auction> auctionResults = auctionRepo.findAllByItemIn(selectedCartItems.stream().map(Cart::getItem)
				.filter(item -> item.getSellType() == Item.SellType.AUCTION).toList());

		// ✅ Retrieve highest bids and store them in the map
		for (Auction auction : auctionResults) {
			Double maxBid = auctionTrackRepository.findMaxPriceByAuctionID(auction.getAuctionID());
			auctionMaxBids.put(auction.getItem().getItemID(), maxBid != null ? maxBid : auction.getStartPrice());
		}

		// ✅ Pass auction bid prices to the view
		model.addAttribute("auctionMaxBids", auctionMaxBids);

		// ✅ Calculate subtotal using the correct highest bid for auction items
		double auctionAdjustedSubtotal = selectedCartItems.stream().mapToDouble(cart -> {
			if (cart.getItem().getSellType() != null && cart.getItem().getSellType() == Item.SellType.AUCTION) {
				Double highestBid = auctionMaxBids.get(cart.getItem().getItemID());
				return cart.getQuantity() * (highestBid != null ? highestBid : cart.getItem().getPrice());
			}
			return cart.getQuantity() * cart.getItem().getPrice();
		}).sum();

		model.addAttribute("cartItems", selectedCartItems);
		model.addAttribute("subtotal", auctionAdjustedSubtotal);
		model.addAttribute("cartCount", cartItems.size());
		model.addAttribute("deliveryFee", deliveryFee);
		model.addAttribute("userAddresses", userAddresses); // ✅ Pass addresses to the view

		return "proceedCheckout"; // ✅ Ensure this Thymeleaf file exists
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

		// ✅ Retrieve & Validate sellerID
		Long sellerID;
		try {
			sellerID = Long.parseLong(requestData.get("sellerID"));
		} catch (NumberFormatException e) {
			response.put("success", false);
			response.put("message", "Invalid seller ID.");
			return response;
		}

		// ✅ Retrieve & Validate Payment Method
		String paymentMethod = requestData.get("paymentMethod");
		if (paymentMethod == null || paymentMethod.isEmpty()) {
			response.put("success", false);
			response.put("message", "Payment method is required.");
			return response;
		}

		// ✅ Fetch Address from Request
		Long addressID;
		try {
			addressID = Long.parseLong(requestData.get("addressID"));
		} catch (NumberFormatException e) {
			response.put("success", false);
			response.put("message", "Invalid address ID.");
			return response;
		}

		// ✅ Validate Address
		Optional<Address> addressOptional = addressRepository.findById(addressID);
		if (addressOptional.isEmpty()) {
			response.put("success", false);
			response.put("message", "Selected address does not exist.");
			return response;
		}
		Address selectedAddress = addressOptional.get();

		// ✅ Fetch & Filter Cart Items for the Selected Seller
		List<Cart> cartItems = cartRepository.findByUser(buyer);
		List<Cart> selectedCartItems = cartItems.stream()
				.filter(cart -> cart.getItem().getSeller().getUserID().equals(sellerID)).collect(Collectors.toList());

		if (selectedCartItems.isEmpty()) {
			response.put("success", false);
			response.put("message", "No items found for this seller.");
			return response;
		}

		// ✅ Check if any item has insufficient stock before proceeding
		for (Cart cart : selectedCartItems) {
			Item item = cart.getItem();
			if (item.getQuality() < cart.getQuantity()) {
				response.put("success", false);
				response.put("message", "Insufficient stock for item: " + item.getItemName());
				return response;
			}
		}

		// ✅ Get & Validate Delivery Fee
		double deliveryFee;
		try {
			deliveryFee = Double.parseDouble(requestData.getOrDefault("deliveryFee", "5.00"));
		} catch (NumberFormatException e) {
			response.put("success", false);
			response.put("message", "Invalid delivery fee.");
			return response;
		}

		// ✅ Calculate Total Price
		// ✅ Fetch the highest bids for auction items
		Map<Long, Double> auctionMaxBids = new HashMap<>();
		List<Auction> auctionResults = auctionRepo.findAllByItemIn(selectedCartItems.stream().map(Cart::getItem)
				.filter(item -> item.getSellType() == Item.SellType.AUCTION).toList());

		for (Auction auction : auctionResults) {
			Double maxBid = auctionTrackRepository.findMaxPriceByAuctionID(auction.getAuctionID());
			auctionMaxBids.put(auction.getItem().getItemID(), maxBid != null ? maxBid : auction.getStartPrice());
		}

		// ✅ Calculate Total Price (Adjust for Auctions)
		double subtotal = selectedCartItems.stream().mapToDouble(cart -> {
			if (cart.getItem().getSellType() == Item.SellType.AUCTION) {
				return cart.getQuantity()
						* auctionMaxBids.getOrDefault(cart.getItem().getItemID(), cart.getItem().getPrice());
			} else {
				return cart.getQuantity() * cart.getItem().getPrice();
			}
		}).sum();

		double totalPrice = subtotal + deliveryFee;

		// ✅ Create & Save Receipt
		Receipt receipt = new Receipt();
		receipt.setBuyer(buyer);
		receipt.setSeller(selectedCartItems.get(0).getItem().getSeller());
		receipt.setTotalPrice(totalPrice);
		receipt.setDeliFee(deliveryFee);
		receipt = receiptRepository.save(receipt);

		// ✅ Create & Save Orders & Reduce Item Stock
		for (Cart cart : selectedCartItems) {
			Item item = cart.getItem();
			item.setQuality(item.getQuality() - cart.getQuantity()); // ✅ Reduce stock
			itemRepository.save(item); // ✅ Save updated stock

			// ✅ Get the correct price for this item (auction or regular)
			Double highestBid = auctionMaxBids.get(cart.getItem().getItemID());
			double finalPrice = highestBid != null ? highestBid : cart.getItem().getPrice();

			// ✅ Create & Save Orders
			Order order = new Order();
			order.setReceipt(receipt);
			order.setBuyer(buyer);
			order.setSeller(cart.getItem().getSeller());
			order.setItem(item);
			order.setQuantity(cart.getQuantity());
			order.setPrice(finalPrice); // ✅ Store the actual highest bid
			orderRepository.save(order);
		}

		// ✅ Create & Save Delivery with the Selected Address
		Delivery delivery = new Delivery();
		delivery.setReceipt(receipt);
		delivery.setStatus(Delivery.DeliveryStatus.PENDING);
		delivery.setAddress(selectedAddress);
		delivery.setUpdatedAt(LocalDateTime.now()); // ✅ Set updated date to now
		delivery = deliveryRepository.save(delivery);

		// ✅ Insert a new record into DeliTrack table for the initial order
		DeliTrack deliTrack = new DeliTrack(delivery, Delivery.DeliveryStatus.PENDING,
				"Order placed and is now pending");
		deliTrackRepository.save(deliTrack); // ✅ Save tracking history

		// ✅ Create & Save Payment
		Payment payment = new Payment();
		payment.setReceipt(receipt);
		payment.setUser(buyer);
		payment.setAmount(BigDecimal.valueOf(totalPrice));
		payment.setPaymentMethod(paymentMethod);
		payment.setPaymentStatus("PENDING");
		paymentRepository.save(payment);

		// ✅ Remove Ordered Items from Cart
		cartRepository.deleteAll(selectedCartItems);

		// ✅ Create Notification for the Seller
		User seller = selectedCartItems.get(0).getItem().getSeller();
		Notification sellerNotification = new Notification();
		sellerNotification.setUser(seller); // Seller receives the notification
		sellerNotification.setSender(buyer); // Buyer placed the order
		sellerNotification.setReceipt(receipt); // ✅ Set Receipt reference
		sellerNotification
				.setNotiText("You have a new order from " + buyer.getUsername() + ". Click to view order details.");
		sellerNotification.setNotiType("NEW_ORDER");
		notificationRepository.save(sellerNotification);

		response.put("success", true);
		response.put("message", "Order placed successfully!");
		return response;
	}

}