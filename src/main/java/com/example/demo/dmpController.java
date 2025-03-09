package com.example.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.entity.Announcement;
import com.example.demo.entity.Cart;
import com.example.demo.entity.Category;
import com.example.demo.entity.Item;
import com.example.demo.entity.User;
import com.example.demo.entity.Wishlist;
import com.example.demo.repository.AnnouncementRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.ViewRepository;
import com.example.demo.repository.WishlistRepository;

import jakarta.servlet.http.HttpSession;

//Idk when and why but this code decided itself to become global
@Controller
public class dmpController {

	@Autowired
	CartRepository cartRepo;
	@Autowired
	private WishlistRepository wishlistRepo;
	@Autowired
	private AnnouncementRepository announcementRepository;
	@Autowired
	private ViewRepository viewRepo;

	@GetMapping("/viewItem")
	public String viewItem() {
		return "viewSale";

	}

	@GetMapping("/viewProfile")
	public String viewProfile() {
		return "viewprofile";

	}

	@GetMapping("/home")
	public String viewHome(HttpSession session, Model model) {
		User user = (User) session.getAttribute("user");
		Set<Item> recommendedItems = new HashSet<>(); // ✅ Use a Set to avoid duplicates
		List<Long> wishlistedItemIds = new ArrayList<>();
		if (user != null) {
			System.out.println("Fetching recommendations for user ID: " + user.getUserID());

			List<Item> byCategory = itemRepo.findRecommendedItemsByCategory(user.getUserID());
			List<Item> byTag = itemRepo.findRecommendedItemsByTag(user.getUserID());

			System.out.println("Category-based recommendations before filtering: " + byCategory.size());
			System.out.println("Tag-based recommendations before filtering: " + byTag.size());

			// ✅ Fetch items in the user's cart
			List<Item> cartItems = cartRepo.findByUser(user).stream().map(Cart::getItem).collect(Collectors.toList());

			System.out.println("Items in cart: " + cartItems.size());

			// ✅ Remove items that are already in the cart
			byCategory.removeAll(cartItems);
			byTag.removeAll(cartItems);

			System.out.println("Category-based recommendations after filtering: " + byCategory.size());
			System.out.println("Tag-based recommendations after filtering: " + byTag.size());

			recommendedItems.addAll(byCategory);
			recommendedItems.addAll(byTag);

			wishlistedItemIds = wishlistRepo.findItemIdsByUser(user.getUserID());

			// ✅ If no recommendations found, show latest 10 items (same as non-logged-in
			// users)
			if (recommendedItems.isEmpty()) {
				System.out.println("No recommendations found, showing latest items instead.");
				List<Item> latestItems = itemRepo.findLatestItems(PageRequest.of(0, 10)).getContent();
				recommendedItems.addAll(latestItems);
			}
		} else {
			// ✅ Fetch only the latest 10 items for guests
			List<Item> latestItems = itemRepo.findLatestItems(PageRequest.of(0, 10)).getContent();
			recommendedItems.addAll(latestItems);
		}

		List<Announcement> announcements = announcementRepository.findAllByOrderByPriorityDescCreatedAtDesc();
		model.addAttribute("announcements", announcements);
		model.addAttribute("recommendedItems", new ArrayList<>(recommendedItems)); // ✅ Convert Set back to List
		model.addAttribute("wishlistedItemIds", wishlistedItemIds);
		return "home";
	}

	@GetMapping("/test")
	public String test() {
		return "test";

	}

	@GetMapping("/faq")
	public String faq() {
		return "FAQ";

	}

	@GetMapping("/contact")
	public String contact() {
		return "contactus";

	}
	// Site that has header footers

	@GetMapping("/viewBuyer")
	public String viewBuyer() {
		return "buyer";

	}

	@GetMapping("/wishList")
	public String viewWishlist(HttpSession session, Model model) {
		User user = (User) session.getAttribute("user");

		if (user == null) {
			return "redirect:/login"; // Redirect if not logged in
		}

		// ✅ Fetch all wishlisted items for the user
		List<Wishlist> wishlistedItems = wishlistRepo.findByUser(user).stream()
				.filter(wishlist -> wishlist.getItem().getSellType() != Item.SellType.AUCTION).toList();

		// ✅ Fetch all items in the user's cart
		List<Item> cartItems = cartRepo.findByUser(user).stream().map(Cart::getItem).collect(Collectors.toList());

		model.addAttribute("wishlistedItems", wishlistedItems);
		model.addAttribute("cartItems", cartItems); // ✅ Add cart items to the model
		return "wish"; // Ensure Thymeleaf file is named `wishlist.html`
	}

	@GetMapping("/changePassword")
	public String changePassword() {
		return "changepassword";
	}

	@GetMapping("/trackDelivery")
	public String trackDelivery() {
		return "deliverytrack";
	}

	@GetMapping("/signup")
	public String signupchoice() {
		return "SignUpChoice";
	}

	@Autowired
	private CategoryRepository categoryRepo;
	@Autowired
	private ItemRepository itemRepo;

	// This will load categories for all pages
	@ModelAttribute("categories")
	public List<Category> loadCategories() {
		List<Category> parentCategories = categoryRepo.findByParentCategoryIsNull();
		parentCategories.forEach(this::loadSubcategories);
		return parentCategories;
	}

	private void loadSubcategories(Category category) {
		List<Category> subcategories = categoryRepo.findByParentCategory(category);
		subcategories.forEach(this::loadSubcategories);
		category.setSubcategories(subcategories);
	}

	@ModelAttribute("placeholderitems")
	public List<Item> loadItems() {
		// Fetch most viewed and latest items
		List<Item> mostViewed = viewRepo.findMostViewedItems(PageRequest.of(0, 8)).getContent();
		List<Item> latest = itemRepo.findLatestItems(PageRequest.of(0, 8)).getContent();

		// Debugging: Print list sizes
		System.out.println("Most Viewed Count: " + mostViewed.size());
		System.out.println("Latest Count: " + latest.size());

		// Merge lists while maintaining priority (most viewed first)
		LinkedHashSet<Item> sortedItems = new LinkedHashSet<>();
		sortedItems.addAll(mostViewed);
		sortedItems.addAll(latest);

		List<Item> result = new ArrayList<>(sortedItems);

		// If we still don't have 8 items, fetch additional latest items
		if (result.size() < 8) {
			int missing = 8 - result.size();
			List<Item> extraLatest = itemRepo.findLatestItems(PageRequest.of(1, missing)).getContent();
			result.addAll(extraLatest);
		}

		// Reverse the order before returning
		Collections.reverse(result);

		// Ensure exactly 8 items are returned
		if (result.size() > 8) {
			result = result.subList(0, 8);
		}

		// Debugging: Print final result size
		System.out.println("Final Loaded Items Count: " + result.size());

		return result;
	}

}
