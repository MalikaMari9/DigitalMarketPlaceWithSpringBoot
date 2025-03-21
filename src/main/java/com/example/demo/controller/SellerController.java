package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.AdminNotification;
import com.example.demo.entity.Category;
import com.example.demo.entity.Item;
import com.example.demo.entity.ItemApproval;
import com.example.demo.entity.Notification;
import com.example.demo.entity.Review;
import com.example.demo.entity.Seller;
import com.example.demo.entity.User;
import com.example.demo.entity.Auction.Auction;
import com.example.demo.repository.AdminNotificationRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ItemApprovalRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.SellerRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WishlistRepository;
import com.example.demo.repository.Auction.AuctionRepository;
import com.example.demo.repository.Auction.AuctionTrackRepository;

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
	@Autowired
	ItemApprovalRepository itemApprovalRepo;
	@Autowired
	WishlistRepository wishlistRepo;
	@Autowired
	AdminNotificationRepository adminNotificationRepo;
	@Autowired
	AuctionTrackRepository auctionTrackRepo;
	@Autowired
	AuctionRepository auctionRepo;
	@Autowired
	private ReviewRepository reviewRepo;

	@Autowired
	private NotificationRepository notiRepo;

	@ModelAttribute("categories")
	public List<Category> loadCategories() {
		List<Category> parentCategories = categoryRepo.findByParentCategoryIsNull();
		parentCategories.forEach(this::loadSubcategories);
		return parentCategories;
	}

	@GetMapping("/pending-sale")
	public String pendingSale(HttpSession session, Model model, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "6") int size,
			@RequestParam(required = false, defaultValue = "") String searchfield,
			@RequestParam(required = false, defaultValue = "itemID") String sortby) {
		User seller = (User) session.getAttribute("user");

		if (seller == null) {
			return "redirect:/login";
		}

		// ✅ Define Sorting Order Dynamically
		Sort sort = Sort.by(Sort.Direction.ASC, "itemID"); // Default sorting

		if ("itemName".equals(sortby)) {
			sort = Sort.by(Sort.Direction.ASC, "itemName");
		} else if ("price".equals(sortby)) {
			sort = Sort.by(Sort.Direction.ASC, "price");
		} else if ("approvalStatus".equals(sortby)) {
			sort = Sort.by(Sort.Direction.ASC, "approve");
		}

		Pageable pageable = PageRequest.of(page, size, sort);

		// ✅ Use updated repository method
		Page<Item> itemPage = itemRepo.findPendingSales(seller.getUserID(), searchfield.isEmpty() ? null : searchfield,
				pageable);

		model.addAttribute("itemPage", itemPage);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", itemPage.getTotalPages());
		model.addAttribute("searchfield", searchfield);
		model.addAttribute("sortby", sortby);

		return "pendingSale";
	}

	@PostMapping("/seller/resubmit/{itemID}")
	@ResponseBody
	public ResponseEntity<?> resubmitItem(@PathVariable Long itemID, HttpSession session) {
		User seller = (User) session.getAttribute("user");

		if (seller == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("❌ Please log in to resubmit.");
		}

		List<Item> itemList = itemRepo.findBySeller_UserID(seller.getUserID());
		int postCount = itemList.size();

		Item item = itemRepo.findById(itemID).orElse(null);

		if (item == null) {
			return ResponseEntity.badRequest().body("❌ Item not found!");
		}

		// ✅ Ensure the item belongs to the seller
		if (!item.getSeller().getUserID().equals(seller.getUserID())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("❌ You can only resubmit your own items.");
		}

		// ✅ Ensure only rejected items can be resubmitted
		if (item.getApprove() != Item.ApprovalStatus.REJECTED) {
			return ResponseEntity.badRequest().body("⚠️ Only rejected items can be resubmitted.");
		}

		// ✅ Update approval status & reset rejection reason
		item.setApprove(Item.ApprovalStatus.PENDING);
		itemRepo.save(item);

		// ✅ Update ItemApproval record
		ItemApproval itemApproval = itemApprovalRepo.findByItem(item);

		if (itemApproval == null) {
			itemApproval = new ItemApproval();
			itemApproval.setItem(item);
		}

		itemApproval.setApprovalDate(null); // ✅ Reset approval date
		itemApproval.setRejectionReason(null); // ✅ Clear rejection reason
		itemApprovalRepo.save(itemApproval);

		return ResponseEntity.ok("✅ Item resubmitted for approval.");
	}

	@GetMapping({ "/viewSeller", "/viewSeller/{sellerID}" })
	public String viewSellerProfile(@PathVariable(value = "sellerID", required = false) Long sellerID,
			HttpSession session, Model model) {
		User seller;

		// ✅ Fetch the seller's profile
		if (sellerID != null) {
			Optional<User> sellerOptional = userRepo.findById(sellerID);
			if (sellerOptional.isEmpty()) {
				return "redirect:/?error=SellerNotFound";
			}
			seller = sellerOptional.get();
		} else {
			seller = (User) session.getAttribute("user");
			if (seller == null) {
				return "redirect:/login?error=notLoggedIn";
			}
		}

		// ✅ Fetch only APPROVED items, sort AVAILABLE first, then SOLD OUT
		List<Item> itemList = itemRepo.findApprovedItemsBySeller(seller.getUserID());

		int postCount = itemList.size();

		// ✅ Separate auction items from normal items (only APPROVED ones)
		List<Item> auctionItemList = itemList.stream().filter(item -> item.getSellType() == Item.SellType.AUCTION)
				.toList();

		// ✅ Fetch auctions related to the seller's auction items
//		List<Auction> auctionResults = auctionRepo.findAllByItemIn(auctionItemList);
		List<Auction> auctionResults = auctionRepo.findAllSortedByItemIn(itemList);
		// ✅ Filter out auctions that haven't started yet

		// ✅ Fetch highest bids for filtered auctions
		Map<Long, Double> auctionMaxBids = new HashMap<>();
		for (Auction auction : auctionResults) {
			Double maxBid = auctionTrackRepo.findMaxPriceByAuctionID(auction.getAuctionID());
			auctionMaxBids.put(auction.getAuctionID(), maxBid != null ? maxBid : auction.getStartPrice());
		}

		// ✅ Fetch wishlist items for the logged-in user
		User user = (User) session.getAttribute("user");
		List<Long> wishlistedItemIds = new ArrayList<>();

		if (user != null) {
			wishlistedItemIds = wishlistRepo.findWishlistedItemIdsByUser(user);
		}

		String businessType = "BUYER"; // Default
		if (seller.getSeller() != null) {
			businessType = seller.getSeller().getBusinessType(); // "C2C" or "B2C"
		}

		// ✅ Fetch reviews for the seller
		List<Review> reviews = new ArrayList<>();
		int reviewCount = 0;
		Double averageRating = 0.0;

		if (seller != null) {
			reviews = reviewRepo.findByReviewedUser(seller.getUserID());
			reviewCount = reviews.size();
			averageRating = reviewRepo.getAverageRating(seller.getUserID());
		}
		// ✅ Add attributes to the model
		model.addAttribute("businessType", businessType);
		model.addAttribute("itemList", itemList); // Normal + Auction items
		model.addAttribute("auctionItemList", auctionItemList); // Auction items only (approved)
		model.addAttribute("auctionResults", auctionResults); // Only auctions that have started
		model.addAttribute("auctionMaxBids", auctionMaxBids);
		model.addAttribute("seller", seller);
		model.addAttribute("reviewCount", reviewCount);
		model.addAttribute("reviews", reviews);
		model.addAttribute("averageRating", averageRating);
		model.addAttribute("wishlistedItemIds", wishlistedItemIds);
		model.addAttribute("postCount", postCount);
		model.addAttribute("isOwnProfile", sellerID == null || (session.getAttribute("user") != null
				&& ((User) session.getAttribute("user")).getUserID().equals(seller.getUserID())));

		return "viewprofile"; // ✅ Thymeleaf template for profile view
	}

	@PostMapping("/addReview/{sellerID}")
	public String addReview(@PathVariable Long sellerID, @RequestParam int rating, @RequestParam String comment,
			HttpSession session) {
		User reviewer = (User) session.getAttribute("user");

		if (reviewer == null) {
			return "redirect:/login?error=notLoggedIn";
		}

		Optional<User> reviewedUserOpt = userRepo.findById(sellerID);
		if (reviewedUserOpt.isEmpty()) {
			return "redirect:/?error=SellerNotFound";
		}

		User reviewedUser = reviewedUserOpt.get();

		// ✅ Prevent users from reviewing themselves
		if (reviewer.getUserID().equals(reviewedUser.getUserID())) {
			return "redirect:/viewSeller/" + sellerID + "?error=CannotReviewYourself";
		}

		// ✅ Check if user already reviewed this seller
		Optional<Review> existingReview = reviewRepo.findByReviewerAndReviewed(reviewer, reviewedUser);
		if (existingReview.isPresent()) {
			return "redirect:/viewSeller/" + sellerID + "?error=AlreadyReviewed";
		}

		// ✅ Create new review
		Review newReview = new Review();
		newReview.setReviewer(reviewer);
		newReview.setReviewed(reviewedUser);
		newReview.setRating(rating);
		newReview.setComment(comment);
		reviewRepo.save(newReview);

		String notiText = reviewer.getUsername() + " has left you a review: \"" + comment + "\"";
		Notification reviewNotification = new Notification(reviewedUser, reviewer, notiText, "NEW_REVIEW");
		notiRepo.save(reviewNotification);

		return "redirect:/viewSeller/" + sellerID;
	}

	@PostMapping("/editReview/{reviewID}")
	public String editReview(@PathVariable Long reviewID, @RequestParam int rating, @RequestParam String comment,
			HttpSession session) {
		User reviewer = (User) session.getAttribute("user");

		if (reviewer == null) {
			return "redirect:/login?error=notLoggedIn";
		}

		Optional<Review> reviewOpt = reviewRepo.findById(reviewID);
		if (reviewOpt.isEmpty()) {
			return "redirect:/?error=ReviewNotFound";
		}

		Review review = reviewOpt.get();

		// ✅ Ensure only the review owner can edit
		if (!review.getReviewer().getUserID().equals(reviewer.getUserID())) {
			return "redirect:/viewSeller/" + review.getReviewed().getUserID() + "?error=UnauthorizedEdit";
		}

		review.setRating(rating);
		review.setComment(comment);
		reviewRepo.save(review);

		return "redirect:/viewSeller/" + review.getReviewed().getUserID();
	}

	@PostMapping("/deleteReview/{reviewID}")
	public String deleteReview(@PathVariable Long reviewID, HttpSession session) {
		User reviewer = (User) session.getAttribute("user");

		if (reviewer == null) {
			return "redirect:/login?error=notLoggedIn";
		}

		Optional<Review> reviewOpt = reviewRepo.findById(reviewID);
		if (reviewOpt.isEmpty()) {
			return "redirect:/?error=ReviewNotFound";
		}

		Review review = reviewOpt.get();

		// ✅ Ensure only the review owner can delete
		if (!review.getReviewer().getUserID().equals(reviewer.getUserID())) {
			return "redirect:/viewSeller/" + review.getReviewed().getUserID() + "?error=UnauthorizedDelete";
		}

		reviewRepo.delete(review);

		return "redirect:/viewSeller/" + review.getReviewed().getUserID();
	}

	// Warehouse

	@GetMapping("/warehouseTrack")
	public String viewWarehouse(HttpSession session, Model model, @RequestParam(defaultValue = "0") int page, // ✅
																												// Default
																												// page
																												// = 0
			@RequestParam(defaultValue = "6") int size, // ✅ Default 6 items per page
			@RequestParam(required = false, defaultValue = "") String searchfield, // ✅ Search query
			@RequestParam(required = false, defaultValue = "itemID") String sortby // ✅ Default sorting by Item ID
	) {
		User seller = (User) session.getAttribute("user");

		if (seller == null) {
			return "redirect:/login";
		}

		Pageable pageable = PageRequest.of(page, size);

		// ✅ Allow sorting even if search is empty
		if (searchfield.isEmpty()) {
			searchfield = null;
		}

		// ✅ Fetch paginated, searchable & sortable warehouse items
		Page<Item> itemPage = itemRepo.findWarehouseItems(seller.getUserID(), searchfield, sortby, pageable);

		model.addAttribute("itemPage", itemPage);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", itemPage.getTotalPages());
		model.addAttribute("searchfield", searchfield != null ? searchfield : "");
		model.addAttribute("sortby", sortby);

		return "warehouseTrack";
	}

	@PostMapping("/update-stock/{itemID}")
	public String updateStock(@PathVariable Long itemID, @RequestParam("stock") int newStock, HttpSession session) {
		User seller = (User) session.getAttribute("user");

		if (seller == null) {
			return "redirect:/login";
		}

		Optional<Item> optionalItem = itemRepo.findById(itemID);

		if (optionalItem.isPresent()) {
			Item item = optionalItem.get();

			// ✅ Ensure the item belongs to the seller
			if (!item.getSeller().getUserID().equals(seller.getUserID())) {
				return "redirect:/warehouseTrack?error=Unauthorized";
			}

			item.setQuality(newStock);

			// ✅ Automatically set to inactive if stock is 0
			if (newStock == 0) {
				item.setStat(Item.Status.SOLD);
			} else {
				item.setStat(Item.Status.AVAILABLE);
			}

			itemRepo.save(item);
		}

		return "redirect:/warehouseTrack";
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

		AdminNotification notification = new AdminNotification();
		notification.setType(AdminNotification.NotificationType.SELLER_APPROVAL);
		notification.setMessage("New seller '" + businessName + "' awaiting approval.");
		notification.setStatus(AdminNotification.NotificationStatus.UNREAD);
		adminNotificationRepo.save(notification); // Save notification

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
