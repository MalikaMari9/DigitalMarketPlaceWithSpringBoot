package com.example.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.entity.Category;
import com.example.demo.repository.CategoryRepository;

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

	// Site that has header footers

	@GetMapping("/addressBook")
	public String addressBook() {
		return "addressbook";

	}

	@GetMapping("/viewBuyer")
	public String viewBuyer() {
		return "buyer";

	}

	@GetMapping("/viewSeller")
	public String viewSeller() {
		return "viewprofile";

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

	@Autowired
	private CategoryRepository categoryRepo;

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

}
