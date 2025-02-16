package com.example.demo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.entity.Cart;
import com.example.demo.entity.Category;
import com.example.demo.entity.Item;
import com.example.demo.entity.User;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ItemRepository;

import jakarta.servlet.http.HttpSession;

//Idk when and why but this code decided itself to become global
@Controller
public class dmpController {

	@Autowired
	CartRepository cartRepo;

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

		model.addAttribute("recommendedItems", new ArrayList<>(recommendedItems)); // ✅ Convert Set back to List
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

	@GetMapping("/addressBook")
	public String addressBook() {
		return "addressbook";

	}

	@GetMapping("/viewBuyer")
	public String viewBuyer() {
		return "buyer";

	}

	@GetMapping("/wishList")
	public String wishList() {
		return "wish";

	}

	@GetMapping("/editProfile")
	public String editProfile() {
		return "editprofile";
	}

	@GetMapping("/changePassword")
	public String changePassword() {
		return "changepassword";
	}

	@GetMapping("/orderHistory")
	public String orderHistory() {
		return "orderhistory";
	}

	@GetMapping("/saleHistory")
	public String saleHistory() {
		return "saleHistory";
	}

	@GetMapping("/auctionHistory")
	public String auctionHistory() {
		return "auctionHistory";
	}

	@GetMapping("/confirmDelivery")
	public String confirmDelivery() {
		return "confirmDelivery";
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
		List<Item> items = itemRepo.findAll();

		// Debugging: Print items to the console
		System.out.println("Loaded Items from DB: " + items);

		return items;
	}

}
