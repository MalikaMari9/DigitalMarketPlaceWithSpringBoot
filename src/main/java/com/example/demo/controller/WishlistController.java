package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Cart;
import com.example.demo.entity.Item;
import com.example.demo.entity.User;
import com.example.demo.entity.Wishlist;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.WishlistRepository;
import com.example.demo.repository.Auction.AuctionTrackRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@RestController
@Controller
public class WishlistController {

	@Autowired
	private WishlistRepository wishlistRepo;

	@Autowired
	private ItemRepository itemRepo;

	@Autowired
	private CartRepository cartRepo;

	@Autowired
	private AuctionTrackRepository auctionTrackRepo;

	// ✅ Toggle Wishlist (Add/Remove)
	@PostMapping("/wishlist/toggle/{itemID}")
	@ResponseBody
	@Transactional // ✅ Ensures delete operation completes properly
	public Map<String, Object> toggleWishlist(@PathVariable Long itemID, HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		User user = (User) session.getAttribute("user");

		if (user == null) {
			response.put("success", false);
			response.put("message", "User not logged in.");
			return response;
		}

		Item item = itemRepo.findById(itemID).orElse(null);
		if (item == null) {
			response.put("success", false);
			response.put("message", "Item not found.");
			return response;
		}

		Optional<Wishlist> wishlistEntry = wishlistRepo.findByUserAndItem(user, item);

		if (wishlistEntry.isPresent()) {
			wishlistRepo.delete(wishlistEntry.get()); // ✅ Ensures deletion
			response.put("wishlisted", false);
		} else {
			Wishlist newWishlist = new Wishlist(user, item);
			wishlistRepo.save(newWishlist);
			response.put("wishlisted", true);
		}

		int wishlistCount = wishlistRepo.countWishlistByItem(item);
		response.put("success", true);
		response.put("wishlistCount", wishlistCount);

		return response;
	}

	@PostMapping("/wishlist/addToCart/{itemID}")
	@Transactional
	public ResponseEntity<String> addWishlistItemToCart(@PathVariable Long itemID, HttpSession session) {
		User user = (User) session.getAttribute("user");

		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("❌ User not logged in.");
		}

		Optional<Item> itemOpt = itemRepo.findById(itemID);
		if (itemOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("❌ Item not found.");
		}

		Item item = itemOpt.get();

		// ✅ Check if item is already in the cart
		Optional<Cart> existingCartItem = cartRepo.findFirstByUserAndItem(user, item);
		if (existingCartItem.isPresent()) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("⚠️ Item is already in your cart.");
		}

		// ✅ Add to cart if it's not already there
		Cart cart = new Cart();
		cart.setItem(item);
		cart.setUser(user);
		cart.setQuantity(1); // Default quantity = 1
		cart.setCreatedAt(LocalDateTime.now());
		cartRepo.save(cart);

		return ResponseEntity.ok("✅ Item added to cart from wishlist.");
	}

}
