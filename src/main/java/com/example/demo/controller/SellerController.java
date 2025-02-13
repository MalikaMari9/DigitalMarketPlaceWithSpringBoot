package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Category;
import com.example.demo.entity.Item;
import com.example.demo.entity.Seller;
import com.example.demo.entity.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.SellerRepository;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class SellerController {

	private static final String UPLOAD_DIR = "src/main/resources/static/Images/NRC/";

	@Autowired
	private ItemRepository itemRepo;
	@Autowired
	private CategoryRepository categoryRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private SellerRepository sellerRepo;

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

	// Seller Registration
	@GetMapping("/SellerRegister")
	public String showSellerRegistrationForm() {
		return "SellerRegisteration";
	}

	@GetMapping("/getNames")
	@ResponseBody
	public List<String> getNamesByCityCode(@RequestParam("cityCode") Integer cityCode) {
		System.out.print(sellerRepo.findNamesByCityCode(cityCode));
		return sellerRepo.findNamesByCityCode(cityCode);
	}

	// Handle Seller Registration
	@PostMapping("/SellerRegister")
	public String registerSeller(@RequestParam("bsn") String businessName,
			@RequestParam("businessType") String businessType, @RequestParam("email") String email,
			@RequestParam("phone") String phone, @RequestParam("psw") String password,
			@RequestParam("name") String name, @RequestParam("dob") String dob,
			@RequestParam("citycode") String cityCode, @RequestParam("name_en") String nameEn,
			@RequestParam("card_type") String cardType, @RequestParam("nrc_code") String nrcCode,
			@RequestParam("nrc_front") MultipartFile nrcFront, @RequestParam("nrc_back") MultipartFile nrcBack,
			HttpServletRequest request, Model model) throws NoSuchAlgorithmException, IOException {

		// Check if username (business name) already exists
		if (userRepo.findByUsername(businessName).isPresent()) {
			model.addAttribute("usernameError", "Business name is already taken");
			return "SellerRegisteration";
		}

		// Combine NRC parts into a single string
		String fullNrc = cityCode + "/" + nameEn + "(" + cardType + ")" + nrcCode;

		// Create new User entity
		User user = new User();
		user.setUsername(businessName); // Business name = username
		user.setPassword(hashPassword(password)); // Hash password
		user.setEmail(email);
		user.setPhone(phone);
		user.setDob(LocalDate.parse(dob)); // Convert DOB to LocalDate
		user.setRole("SELLER"); // Role set as SELLER

		// Save user in user_tbl
		user = userRepo.save(user);

		// Handle NRC Image Uploads
		String frontFileName = saveFile(nrcFront);
		String backFileName = saveFile(nrcBack);

		// Create Seller entity and set userID
		Seller seller = new Seller();
		seller.setUser(user);
		seller.setBusinessName(businessName);
		seller.setBusinessType(businessType);
		seller.setName(name);
		seller.setNrcNo(fullNrc); // Save the full NRC string
		seller.setNrcFront(frontFileName);
		seller.setNrcBack(backFileName);
		seller.setApproval("pending"); // Default approval status

		// Save seller in seller_tbl
		sellerRepo.save(seller);

		// Store session
		HttpSession session = request.getSession();
		session.setAttribute("user", user);

		return "redirect:/login"; // Redirect to home after successful registration
	}

	// Password Hashing Method (SHA-256)
	private String hashPassword(String password) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] hashedBytes = md.digest(password.getBytes());

		// Convert bytes to hex format
		StringBuilder hexString = new StringBuilder();
		for (byte b : hashedBytes) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}

	// Save NRC Images and return file names
	private String saveFile(MultipartFile file) throws IOException {
		if (file.isEmpty()) {
			return "default.png"; // Default image if no file uploaded
		}
		String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
		Path path = Paths.get(UPLOAD_DIR + fileName);
		Files.createDirectories(path.getParent());
		Files.write(path, file.getBytes());
		return fileName;
	}

	private void loadSubcategories(Category category) {
		List<Category> subcategories = categoryRepo.findByParentCategory(category);
		subcategories.forEach(this::loadSubcategories); // Recursive call to load deeper levels
		category.setSubcategories(subcategories);
	}

}
