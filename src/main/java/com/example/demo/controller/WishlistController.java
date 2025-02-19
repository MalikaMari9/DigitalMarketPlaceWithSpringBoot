package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Item;
import com.example.demo.entity.User;
import com.example.demo.entity.Wishlist;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.WishlistRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@RestController

public class WishlistController {

	@Autowired
	private WishlistRepository wishlistRepo;

	@Autowired
	private ItemRepository itemRepo;

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
}
