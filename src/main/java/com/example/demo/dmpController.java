package com.example.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.entity.Category;
import com.example.demo.entity.Item;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ItemRepository;

//Idk when and why but this code decided itself to become global
@Controller
public class dmpController {
	@GetMapping("/viewItem")
	public String viewItem() {
		return "viewSale";

	}

	@GetMapping("/viewProfile")
	public String viewProfile() {
		return "viewprofile";

	}

	@GetMapping("/home")
	public String viewHome() {
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

	@GetMapping("/register")
	public String reg() {
		return "Registeration.html";
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
