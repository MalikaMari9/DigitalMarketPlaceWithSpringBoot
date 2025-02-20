package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.example.demo.entity.Cart;
import com.example.demo.entity.Item;
import com.example.demo.entity.User;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller // ✅ Use @Controller for Thymeleaf templates
@RequestMapping("/cart")
@SessionAttributes("user") // ✅ Ensures session contains "user"
public class CartController {

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private UserRepository userRepository;

	// ✅ Add item to cart
	@PostMapping("/add")
	@Transactional
	public @ResponseBody Response addToCart(@RequestBody CartRequest request, HttpSession session) {
		// ✅ Retrieve the logged-in user from session
		User user = (User) session.getAttribute("user");

		if (user == null) {
			System.out.println("❌ User not logged in.");
			return new Response(false, "User not logged in.");
		}

		System.out.println("📥 Received Add to Cart request: " + request.getItemId() + ", User: " + user.getUserID());

		Optional<Item> itemOpt = itemRepository.findById(request.getItemId());

		if (itemOpt.isEmpty()) {
			System.out.println("❌ Invalid item.");
			return new Response(false, "Invalid item.");
		}

		Item item = itemOpt.get();

		if (request.getQuantity() > item.getQuality()) {
			System.out.println("⚠️ Not enough stock.");
			return new Response(false, "Not enough stock available.");
		}

		Cart cart = cartRepository.findFirstByUserAndItem(user, item).orElse(new Cart());

		cart.setItem(item);
		cart.setUser(user);
		cart.setQuantity(cart.getCartID() == null ? request.getQuantity() : cart.getQuantity() + request.getQuantity());
		cart.setCreatedAt(LocalDateTime.now());

		cartRepository.save(cart);

		int cartCount = cartRepository.countByUser(user);

		System.out.println("✅ Item added to cart. Cart count: " + cartCount);

		return new Response(true, "Item added to cart.", cartCount);
	}

	// ✅ Get cart count for logged-in user
	@GetMapping("/count")
	public ResponseEntity<Map<String, Integer>> getCartCount(HttpSession session) {
		User user = (User) session.getAttribute("user");
		int cartCount = cartRepository.countByUser(user);
		Map<String, Integer> response = new HashMap<>();
		response.put("cartCount", cartCount);
		return ResponseEntity.ok(response);
	}

	// ✅ View cart (Grouped by Seller)
	@GetMapping("/view")
	public String viewCart(ModelMap model, HttpSession session) {
		User user = (User) session.getAttribute("user");
		System.out.print("I am here");
		if (user == null) {
			System.out.print("user is null");
			return "redirect:/loginPage"; // Change this to your actual login page route
		} else {
			System.out.print("user is not null");
		}

		List<Cart> cartItems = cartRepository.findByUser(user);

		if (cartItems == null || cartItems.isEmpty()) {
			model.addAttribute("emptyCartMessage", "Your cart is empty.");
			model.addAttribute("cartCount", 0); // Ensure cartCount is always available
			return "proceedCheckout"; // Thymeleaf file
		}

		// ✅ Group items by seller
		Map<User, List<Cart>> groupedBySeller = cartItems.stream()
				.filter(cart -> cart.getItem() != null && cart.getItem().getSeller() != null)
				.collect(Collectors.groupingBy(cart -> cart.getItem().getSeller()));

		// ✅ Convert to LinkedHashMap to maintain order
		Map<User, List<Cart>> sortedGroupedCart = groupedBySeller.entrySet().stream()
				.sorted(Comparator.comparing(entry -> entry.getKey().getUsername())) // Sort by seller name
				// Alternative: .sorted(Comparator.comparing(entry ->
				// entry.getKey().getUserID())) // Sort by seller ID
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new // ✅
																														// Ensures
																														// order
																														// is
																														// maintained
				));

		model.addAttribute("groupedCart", sortedGroupedCart);
		model.addAttribute("cartCount", cartItems.size()); // ✅ Send `cartCount` to Thymeleaf

		return "proceedCheckout"; // Ensure Thymeleaf file name is correct
	}

	@DeleteMapping("/remove/{cartID}")
	public ResponseEntity<?> removeFromCart(@PathVariable Long cartID) {
		Optional<Cart> cartItem = cartRepository.findById(cartID);

		if (cartItem.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("success", false, "message", "Cart item not found"));
		}

		cartRepository.deleteById(cartID); // ✅ Delete from database
		return ResponseEntity.ok(Map.of("success", true, "message", "Item removed from cart"));
	}

	@PutMapping("/update/{cartID}")
	public ResponseEntity<?> updateCartQuantity(@PathVariable Long cartID, @RequestBody Map<String, Integer> payload) {
		Optional<Cart> cartItemOpt = cartRepository.findById(cartID);

		if (cartItemOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("success", false, "message", "Cart item not found"));
		}

		Cart cartItem = cartItemOpt.get();
		int newQuantity = payload.get("quantity");

		if (newQuantity <= 0) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Quantity must be at least 1"));
		}

		cartItem.setQuantity(newQuantity);
		cartRepository.save(cartItem);

		return ResponseEntity.ok(Map.of("success", true, "message", "Quantity updated"));
	}

	// ✅ DTO for Cart Count Response
	static class CartCountResponse {
		private final int cartCount;

		public CartCountResponse(int cartCount) {
			this.cartCount = cartCount;
		}

		public int getCartCount() {
			return cartCount;
		}
	}

	// ✅ DTOs for request and response
	static class CartRequest {
		private Long itemId;
		private Long userId;
		private int quantity;

		public Long getItemId() {
			return itemId;
		}

		public void setItemId(Long itemId) {
			this.itemId = itemId;
		}

		public Long getUserId() {
			return userId;
		}

		public void setUserId(Long userId) {
			this.userId = userId;
		}

		public int getQuantity() {
			return quantity;
		}

		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}
	}

	static class Response {
		private boolean success;
		private String message;
		private int cartCount;

		public Response(boolean success, String message) {
			this.success = success;
			this.message = message;
		}

		public Response(boolean success, String message, int cartCount) {
			this.success = success;
			this.message = message;
			this.cartCount = cartCount;
		}

		public boolean isSuccess() {
			return success;
		}

		public String getMessage() {
			return message;
		}

		public int getCartCount() {
			return cartCount;
		}
	}
}
