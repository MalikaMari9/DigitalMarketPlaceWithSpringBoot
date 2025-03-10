package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.Seller;
import com.example.demo.entity.User;
import com.example.demo.repository.SellerRepository;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class SellerConversionController {

	private static final String UPLOAD_DIR = "src/main/resources/static/Images/NRC/";

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private SellerRepository sellerRepo;

	// ✅ Buyer → C2C Seller (First-time vs. Returning)
	@GetMapping("/convert-to-seller")
	public String convertToSeller(HttpSession session, RedirectAttributes redirectAttributes) {
		User user = (User) session.getAttribute("user");
		if (user == null) {
			return "redirect:/login";
		}

		// ✅ Check if the user has a previous seller record
		Seller existingSeller = sellerRepo.findByUser(user);

		if (existingSeller != null) {
			// ✅ Directly convert back to seller
			user.setRole("SELLER");
			userRepo.save(user);
			session.setAttribute("user", user); // Update session

			redirectAttributes.addFlashAttribute("success", "You are now a seller again!");
			return "redirect:/sellerDashboard";
		} else {
			// ✅ First-time seller, redirect to registration
			return "redirect:/seller-register";
		}
	}

	// ✅ Convert C2C Seller → Buyer
	@GetMapping("/convert-to-buyer")
	public String convertToBuyer(HttpSession session, RedirectAttributes redirectAttributes) {
		User user = (User) session.getAttribute("user");
		if (user == null) {
			return "redirect:/login";
		}

		Seller seller = sellerRepo.findByUser(user);
		if (seller == null || !"C2C".equalsIgnoreCase(seller.getBusinessType())) {
			redirectAttributes.addFlashAttribute("error", "You cannot switch to buyer.");
			return "redirect:/home";
		}

		// ✅ Remove seller role and revert to Buyer
		user.setRole("BUYER");
		userRepo.save(user);

		session.setAttribute("user", user); // ✅ Update session
		redirectAttributes.addFlashAttribute("success", "You are now a buyer!");

		return "redirect:/home";
	}

	@GetMapping("seller-register")
	public String showRegister() {
		return "BuyerToSellerRegister";
	}

	@PostMapping("/register-seller")
	public String registerSeller(@RequestParam("bsn") String businessName,
			@RequestParam("businessType") String businessType, @RequestParam("name") String name,
			@RequestParam("dob") String dob, @RequestParam("citycode") String cityCode,
			@RequestParam("name_en") String nameEn, @RequestParam("card_type") String cardType,
			@RequestParam("nrc_code") String nrcCode, @RequestParam("nrc_front") MultipartFile nrcFront,
			@RequestParam("nrc_back") MultipartFile nrcBack, HttpServletRequest request,
			RedirectAttributes redirectAttributes) throws IOException {

		// ✅ Get user from session
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");

		if (user == null) {
			return "redirect:/login";
		}

		// ✅ Prevent duplicate registration
		if (sellerRepo.findByUser(user) != null) {
			redirectAttributes.addFlashAttribute("error", "You are already registered as a seller.");
			return "redirect:/home";
		}

		// ✅ Construct NRC
		String fullNrc = cityCode + "/" + nameEn + "(" + cardType + ")" + nrcCode;

		// ✅ Handle NRC image uploads
		String frontFileName = saveFile(nrcFront);
		String backFileName = saveFile(nrcBack);

		// ✅ Create Seller entity
		Seller seller = new Seller(user, businessType, name, fullNrc, backFileName, frontFileName, businessName);
		seller.setApproval("pending"); // ✅ Approval status default to "pending"
		sellerRepo.save(seller);

		// ✅ Update user role in session
		user.setRole("SELLER");
		userRepo.save(user);
		session.setAttribute("user", user);

		redirectAttributes.addFlashAttribute("success", "Seller registration submitted! Awaiting approval.");
		return "redirect:/login";
	}

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

}
