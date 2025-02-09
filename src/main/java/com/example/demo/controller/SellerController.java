package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Category;
import com.example.demo.entity.Item;
import com.example.demo.entity.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ItemRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class SellerController {

	@Autowired
	private ItemRepository itemRepo;
	@Autowired
	private CategoryRepository categoryRepo;

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

	private void loadSubcategories(Category category) {
		List<Category> subcategories = categoryRepo.findByParentCategory(category);
		subcategories.forEach(this::loadSubcategories); // Recursive call to load deeper levels
		category.setSubcategories(subcategories);
	}

}
