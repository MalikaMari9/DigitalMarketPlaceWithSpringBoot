package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Category;
import com.example.demo.entity.Item;
import com.example.demo.entity.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class SellerController {

	@Autowired
	private ItemRepository itemRepo;
	@Autowired
	private CategoryRepository categoryRepo;
	@Autowired
	private UserRepository userRepo;

	@ModelAttribute("categories")
	public List<Category> loadCategories() {
		List<Category> parentCategories = categoryRepo.findByParentCategoryIsNull();
		parentCategories.forEach(this::loadSubcategories);
		return parentCategories;
	}

	@GetMapping("/pending-sale")
	public String pendingSale(HttpSession session, Model model, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "6") int size) {
		User seller = (User) session.getAttribute("user");

		// ✅ Prevent NullPointerException if user is not logged in
		if (seller == null) {
			return "redirect:/loginPage"; // Redirect to login page
		}

		Long sellerID = seller.getUserID();
		Pageable pageable = PageRequest.of(page, size);
		Page<Item> itemPage = itemRepo.findBySeller_UserIDOrderByApproveDesc(sellerID, pageable); // ✅ Ensure method
		model.addAttribute("itemPage", itemPage); // ✅ Pass paginated items
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", itemPage.getTotalPages()); // name is
		// correct

		return "pendingSale";
	}

	@GetMapping({ "/viewSeller", "/viewSeller/{sellerID}" })
	public String viewSellerProfile(@PathVariable(value = "sellerID", required = false) Long sellerID,
			HttpSession session, Model model) {
		User seller;

		// If sellerID is provided in the URL, fetch that seller's profile
		if (sellerID != null) {
			Optional<User> sellerOptional = userRepo.findById(sellerID);
			if (sellerOptional.isEmpty()) {
				return "redirect:/?error=SellerNotFound";
			}
			seller = sellerOptional.get();
		}
		// If no sellerID is provided, assume the logged-in user
		else {
			seller = (User) session.getAttribute("user");
			if (seller == null) {
				return "redirect:/loginPage?error=notLoggedIn"; // Redirect to login if user is not logged in
			}
		}

		// Fetch items sold by the seller
		List<Item> itemList = itemRepo.findBySeller_UserID(seller.getUserID());

		// Pass data to Thymeleaf
		model.addAttribute("itemList", itemList);
		model.addAttribute("seller", seller);
		model.addAttribute("isOwnProfile", sellerID == null || (session.getAttribute("user") != null
				&& ((User) session.getAttribute("user")).getUserID().equals(seller.getUserID())));

		return "viewprofile"; // ✅ Thymeleaf template for profile view
	}

	@GetMapping("/chat")
	public String chatPage() {
		return "chat"; // This will return chat.html from templates/
	}

	@GetMapping("/session-user")
	public ResponseEntity<?> getSessionUser(HttpSession session) {
		Long userID = (Long) session.getAttribute("user");
		if (userID == null) {
			return ResponseEntity.status(401).body(Map.of("error", "User not logged in"));
		}

		return userRepo.findById(userID)
				.map(user -> ResponseEntity.ok(Map.of("userID", user.getUserID(), "username", user.getUsername())))
				.orElse(ResponseEntity.status(404).body(Map.of("error", "User not found")));
	}

	private void loadSubcategories(Category category) {
		List<Category> subcategories = categoryRepo.findByParentCategory(category);
		subcategories.forEach(this::loadSubcategories); // Recursive call to load deeper levels
		category.setSubcategories(subcategories);
	}

}
