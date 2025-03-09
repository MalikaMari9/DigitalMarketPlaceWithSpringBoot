package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.Delivery;
import com.example.demo.entity.Item;
import com.example.demo.entity.Item.ApprovalStatus;
import com.example.demo.entity.ItemApproval;
import com.example.demo.entity.ItemImage;
import com.example.demo.entity.Notification;
import com.example.demo.entity.Order;
import com.example.demo.entity.Review;
import com.example.demo.entity.Seller;
import com.example.demo.entity.User;
import com.example.demo.entity.Auction.Auction;
import com.example.demo.entity.Auction.AuctionTrack;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.DeliveryRepository;
import com.example.demo.repository.ItemApprovalRepository;
import com.example.demo.repository.ItemImageRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.SellerRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.ViewRepository;
import com.example.demo.repository.WishlistRepository;
import com.example.demo.repository.Auction.AuctionRepository;
import com.example.demo.repository.Auction.AuctionTrackRepository;
import com.example.demo.repository.tag.ItemTagRepository;
import com.example.demo.repository.tag.TagRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

	@Autowired
	ItemRepository itemRepo;

	@Autowired
	CategoryRepository categoryRepo;

	@Autowired
	CartRepository cartRepo;

	@Autowired
	WishlistRepository wishlistRepo;

	@Autowired
	UserRepository userRepo;

	@Autowired
	AuctionRepository auctionRepo;
	@Autowired
	AuctionTrackRepository auctionTrackRepo;
	@Autowired
	ItemImageRepository itemImageRepo;
	@Autowired
	TagRepository tagRepo;
	@Autowired
	ItemTagRepository itemTagRepo;
	@Autowired
	ViewRepository viewRepo;
	@Autowired
	ItemApprovalRepository itemApprovalRepo;
	@Autowired
	SellerRepository sellerRepo;
	@Autowired
	NotificationRepository notificationRepo;
	@Autowired
	OrderRepository orderRepo;
	@Autowired
	DeliveryRepository deliRepo;
	@Autowired
	AddressRepository addressRepository;
	@Autowired
	ViewRepository viewRepository;
	@Autowired
	private ReviewRepository reviewRepo;

	private final String BASE_DIR = "src/main/resources/static/Image/Item/";

	@GetMapping("/admin/viewDashboard")
	public String viewDashboard(Model model) {
		// ✅ Fetch Total Orders
		Long totalOrders = orderRepo.countTotalOrders();

		// ✅ Fetch Total Sales (Sum of all orders' price * quantity)
		Double totalSales = orderRepo.sumTotalSales();
		if (totalSales == null) {
			totalSales = 0.0;
		}

		// ✅ Fetch Total Visits (Total item views)
		Long totalVisits = viewRepository.countTotalViews();
		if (totalVisits == null) {
			totalVisits = 0L;
		}

		// ✅ Fetch Category Distribution
		List<Object[]> categoryData = itemRepo.countItemsPerCategory();
		List<Map<String, Object>> categoryStats = new ArrayList<>();

		for (Object[] row : categoryData) {
			Map<String, Object> categoryMap = new HashMap<>();
			categoryMap.put("name", row[0]); // Category name
			categoryMap.put("count", row[1]); // Number of items
			categoryStats.add(categoryMap);
		}

		// ✅ Add values to model
		model.addAttribute("totalOrders", totalOrders);
		model.addAttribute("totalSales", totalSales);
		model.addAttribute("totalVisits", totalVisits);
		model.addAttribute("categoryStats", categoryStats);

		return "admin/dashboard"; // Returns the Thymeleaf template
	}

	@GetMapping("/admin/viewDashboard/Catdata")
	@ResponseBody
	public List<Map<String, Object>> getCategoryStats() {
		List<Object[]> categoryData = itemRepo.countItemsPerCategory();
		List<Map<String, Object>> categoryStats = new ArrayList<>();

		for (Object[] row : categoryData) {
			Map<String, Object> categoryMap = new HashMap<>();
			categoryMap.put("name", row[0]); // Category name
			categoryMap.put("count", row[1]); // Number of items
			categoryStats.add(categoryMap);
		}

		return categoryStats;
	}

	@GetMapping("/admin/viewDashboard/categoryData")
	@ResponseBody
	public List<Map<String, Object>> getCategorySalesData() {
		List<Object[]> results = itemRepo.countItemsPerCategory(); // Fetch aggregated category data
		List<Map<String, Object>> categoryData = new ArrayList<>();

		for (Object[] result : results) {
			Map<String, Object> data = new HashMap<>();
			data.put("name", result[0]); // Category Name
			data.put("value", result[1]); // Percentage or count
			// data.put("count", result[2]); // Number of items sold
			categoryData.add(data);
		}

		return categoryData;
	}

	@GetMapping("/admin/viewDashboard/data")
	@ResponseBody
	public List<Map<String, Object>> getMonthlySalesAndViews() {
		List<Map<String, Object>> monthlyData = new ArrayList<>();

		for (int i = 1; i <= 12; i++) {
			Map<String, Object> monthlyStats = new HashMap<>();

			// ✅ Get Monthly Sales
			Double monthlySales = orderRepo.getMonthlySales(i);
			if (monthlySales == null) {
				monthlySales = 0.0;
			}

			// ✅ Get Monthly Views
			Long monthlyViews = viewRepository.getMonthlyViews(i);
			if (monthlyViews == null) {
				monthlyViews = 0L;
			}

			// ✅ Add Data to List
			monthlyStats.put("month", YearMonth.of(YearMonth.now().getYear(), i).getMonth().toString());
			monthlyStats.put("sales", monthlySales);
			monthlyStats.put("views", monthlyViews);
			monthlyData.add(monthlyStats);
		}

		return monthlyData;
	}

	// Approval
	@GetMapping("/admin/approvals")
	public String viewApprovals(Model model, HttpSession session, @RequestParam(defaultValue = "0") int sellerPage,
			@RequestParam(defaultValue = "6") int sellerSize, @RequestParam(defaultValue = "0") int itemPage,
			@RequestParam(defaultValue = "6") int itemSize) { // ✅ Consistent page size

		if (session.getAttribute("admin") == null) { // ✅ Ensure admin is logged in
			return "redirect:/loginPage?error=Session Expired";
		}

		Pageable sellerPageable = PageRequest.of(sellerPage, sellerSize);
		Page<Seller> sellerPageData = sellerRepo.findByApproval("pending", sellerPageable); // ✅ Fetch paginated sellers

		Pageable itemPageable = PageRequest.of(itemPage, itemSize);
		Page<Item> itemPageData = itemRepo.findByApprove(Item.ApprovalStatus.PENDING, itemPageable); // ✅ Fetch
																										// paginated
																										// items

		// ✅ Check if each seller has a main address
		Map<Long, String> sellerHasMainAddress = new HashMap<>();
		for (Seller seller : sellerPageData.getContent()) {
			boolean hasMainAddress = addressRepository.existsByUserAndIsMainAddressTrue(seller.getUser());
			sellerHasMainAddress.put(seller.getSellerID(), hasMainAddress ? "Set" : "Not Set");
		}

		model.addAttribute("pendingSellers", sellerPageData.getContent());
		model.addAttribute("sellerCurrentPage", sellerPage);
		model.addAttribute("sellerTotalPages", sellerPageData.getTotalPages());

		model.addAttribute("pendingItems", itemPageData.getContent());
		model.addAttribute("itemCurrentPage", itemPage);
		model.addAttribute("itemTotalPages", itemPageData.getTotalPages());

		model.addAttribute("sellerHasMainAddress", sellerHasMainAddress);

		return "admin/approvals";
	}

	@GetMapping("/admin/bestSeller")
	@ResponseBody
	public Map<String, Object> getBestSeller() {
		Map<String, Object> response = new HashMap<>();

		List<Object[]> bestSellers = orderRepo.findBestSellerOfMonth();

		System.out.println("🔍 Debug: Best Seller Query Result: " + bestSellers);

		if (!bestSellers.isEmpty()) {
			Object[] bestSeller = bestSellers.get(0); // Get the first result
			response.put("userID", bestSeller[0]); // Seller's userID
			response.put("sellerName", bestSeller[1]); // Seller's username
			response.put("totalSales", bestSeller[2]); // Total Sales
			response.put("salesTarget", "58% of sales target"); // Example Target
		} else {
			response.put("userID", null);
			response.put("sellerName", "No Best Seller");
			response.put("totalSales", 0);
			response.put("salesTarget", "N/A");
		}

		return response;
	}

	@GetMapping("/admin/progressData")
	@ResponseBody
	public Map<String, Double> getProgressData() {
		Map<String, Double> progressData = new HashMap<>();
		Double SALES_GOAL = 4000.0;
		Double PRODUCTS_GOAL = 100.0;
		Double REVENUE_GOAL = 10000.0;
		// Fetching total sales
		Double totalSales = orderRepo.getTotalSalesThisMonth();
		if (totalSales == null)
			totalSales = 0.0;
		double salesProgress = (totalSales / SALES_GOAL) * 100;

		// Fetching total products sold
		Integer totalProducts = orderRepo.getTotalProductsSoldThisMonth();
		if (totalProducts == null)
			totalProducts = 0;
		double productsProgress = (totalProducts.doubleValue() / PRODUCTS_GOAL) * 100;

		// Fetching total revenue
		Double totalRevenue = orderRepo.getTotalRevenueThisMonth();
		if (totalRevenue == null)
			totalRevenue = 0.0;
		double incomeProgress = (totalRevenue / REVENUE_GOAL) * 100;

		// Store progress values
		progressData.put("salesProgress", Math.min(salesProgress, 100)); // Ensure max is 100%
		progressData.put("productsProgress", Math.min(productsProgress, 100));
		progressData.put("incomeProgress", Math.min(incomeProgress, 100));

		return progressData;
	}

	@ModelAttribute("approvalCount")
	public int getApprovalCount() {
		int pendingSellers = sellerRepo.countByApproval("pending"); // ✅ Count pending sellers
		int pendingItems = itemRepo.countByApprove(ApprovalStatus.PENDING); // ✅ Count pending items
		return pendingSellers + pendingItems; // ✅ Total approvals needed
	}

	// ✅ Approve Item
	@PostMapping("/admin/approve/{itemID}")
	@ResponseBody
	public ResponseEntity<?> approveItem(@PathVariable Long itemID) {
		Item item = itemRepo.findById(itemID).orElse(null);

		if (item == null) {
			return ResponseEntity.badRequest().body("❌ Item not found!");
		}

		// ✅ Update Item Approval Status
		item.setApprove(Item.ApprovalStatus.APPROVED);
		itemRepo.save(item);

		// ✅ Check if an approval record already exists
		ItemApproval itemApproval = itemApprovalRepo.findByItem(item);
		if (itemApproval == null) {
			itemApproval = new ItemApproval();
			itemApproval.setItem(item);
		}
		itemApproval.setApprovalDate(LocalDateTime.now());
		itemApproval.setRejectionReason(null); // ✅ No rejection reason for approved items
		itemApprovalRepo.save(itemApproval);

		// ✅ Notify Seller
		User seller = item.getSeller();
		if (seller != null) {
			Notification notification = new Notification(seller, item,
					"Your item '" + item.getItemName() + "' has been approved!", "ITEM_APPROVAL");
			notificationRepo.save(notification);
		}

		return ResponseEntity.ok("✅ Item Approved!");
	}

	// ✅ Reject Item with Reason
	@PostMapping("/admin/reject/{itemID}")
	@ResponseBody
	public ResponseEntity<?> rejectItem(@PathVariable Long itemID, @RequestParam("reason") String reason) {
		Item item = itemRepo.findById(itemID).orElse(null);

		if (item == null) {
			return ResponseEntity.badRequest().body("❌ Item not found!");
		}

		// ✅ Update Item Approval Status
		item.setApprove(Item.ApprovalStatus.REJECTED);
		itemRepo.save(item);

		// ✅ Check if an approval record already exists
		ItemApproval itemApproval = itemApprovalRepo.findByItem(item);
		if (itemApproval == null) {
			itemApproval = new ItemApproval();
			itemApproval.setItem(item);
		}

		itemApproval.setApprovalDate(LocalDateTime.now());
		itemApproval.setRejectionReason(reason);
		itemApprovalRepo.save(itemApproval);

		// ✅ Notify Seller
		User seller = item.getSeller();
		if (seller != null) {
			Notification notification = new Notification(seller, item,
					"Your item '" + item.getItemName() + "' has been rejected. Reason: " + reason, "ITEM_REJECTION");
			notificationRepo.save(notification);
		}

		return ResponseEntity.ok("❌ Item Rejected!");
	}

	@PostMapping("admin/approve-seller/{sellerID}")
	@ResponseBody
	public ResponseEntity<?> approveSeller(@PathVariable Long sellerID) {
		Seller seller = sellerRepo.findById(sellerID).orElse(null);

		if (seller == null) {
			return ResponseEntity.badRequest().body("❌ Seller not found!");
		}

		// ✅ Update Seller Approval Status
		seller.setApproval("accepted");
		seller.setApprovalDate(LocalDateTime.now());

		// ✅ Change User Role to "SELLER"
		User user = seller.getUser();
		if (user != null) {
			user.setRole("SELLER");
			userRepo.save(user);
			Notification notification = new Notification(user, "Your seller account has been approved!",
					"SELLER_APPROVAL");
			notificationRepo.save(notification);
		}

		sellerRepo.save(seller);
		return ResponseEntity.ok("✅ Seller Approved!");
	}

	// ✅ Reject Seller
	@PostMapping("admin/reject-seller/{sellerID}")
	@ResponseBody
	public ResponseEntity<?> rejectSeller(@PathVariable Long sellerID) {
		Seller seller = sellerRepo.findById(sellerID).orElse(null);

		if (seller == null) {
			return ResponseEntity.badRequest().body("❌ Seller not found!");
		}

		// ✅ Update Seller Approval Status
		seller.setApproval("rejected");
		seller.setApprovalDate(LocalDateTime.now());

		User user = seller.getUser();
		if (user != null) {
			Notification notification = new Notification(user, "Your seller application has been rejected.",
					"SELLER_REJECTION");
			notificationRepo.save(notification);
		}

		sellerRepo.save(seller);

		return ResponseEntity.ok("❌ Seller Rejected!");
	}

	@GetMapping("/admin/auctions")
	public String viewAuctions(Model model, HttpSession session, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "6") int size) { // ✅ Page size is consistent with other lists

		if (session.getAttribute("admin") == null) { // ✅ Ensure admin is logged in
			return "redirect:/loginPage?error=Session Expired";
		}

		Pageable pageable = PageRequest.of(page, size);
		Page<Auction> auctionPage = auctionRepo.findAll(pageable); // ✅ Fetch paginated auctions

		model.addAttribute("auctions", auctionPage.getContent()); // ✅ List of auctions
		model.addAttribute("currentPage", page); // ✅ Current page number
		model.addAttribute("totalPages", auctionPage.getTotalPages()); // ✅ Total pages

		return "admin/auctions"; // ✅ Render Thymeleaf template
	}

	@GetMapping("/admin/auction/getTrackings")
	public ResponseEntity<List<Map<String, Object>>> getAuctionTrackings(@RequestParam Long auctionID) {
		Optional<Auction> auctionOpt = auctionRepo.findById(auctionID);

		if (auctionOpt.isEmpty()) {
			return ResponseEntity.badRequest().build();
		}

		Auction auction = auctionOpt.get();
		List<AuctionTrack> tracks = auctionTrackRepo.findByAuctionOrderByCreatedAtDesc(auction);

		List<Map<String, Object>> trackList = new ArrayList<>();
		for (AuctionTrack track : tracks) {
			Map<String, Object> trackInfo = new HashMap<>();
			trackInfo.put("bidderID", track.getUser().getUserID());
			trackInfo.put("bidderName", track.getUser().getUsername());
			trackInfo.put("bidAmount", track.getPrice());
			trackInfo.put("time", track.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

			trackList.add(trackInfo);
		}

		return ResponseEntity.ok(trackList);
	}

	@GetMapping("/admin/delivery")
	public String viewDelivery(HttpSession session, Model model, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "6") int size) { // ✅ Keep size consistent with other pages

		if (session.getAttribute("admin") == null) { // ✅ Ensure only admins can access
			return "redirect:/loginPage?error=Session Expired";
		}

		Pageable pageable = PageRequest.of(page, size);
		Page<Delivery> deliveryPage = deliRepo.findAll(pageable); // ✅ Fetch paginated deliveries

		model.addAttribute("deliveries", deliveryPage.getContent()); // ✅ List of deliveries
		model.addAttribute("currentPage", page); // ✅ Current page number
		model.addAttribute("totalPages", deliveryPage.getTotalPages()); // ✅ Total pages

		return "admin/delivery"; // ✅ Match the Thymeleaf template
	}

	@GetMapping("/admin/items")
	public String viewItems(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "6") int size,
			Model model, HttpSession session) {
		if (session.getAttribute("admin") == null) { // ✅ Check if admin session exists
			return "redirect:/loginPage?error=Session Expired"; // ✅ Redirect to login if session expired
		}
		Pageable pageable = PageRequest.of(page, size);
		Page<Item> itemPage = itemRepo.findAll(pageable);

		model.addAttribute("items", itemPage.getContent()); // ✅ Ensure Thymeleaf gets the correct list
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", itemPage.getTotalPages());

		return "admin/items";
	}

	@DeleteMapping("/admin/delete-item/{itemId}")
	@Transactional
	public ResponseEntity<Map<String, Object>> deleteItem(@PathVariable Long itemId) {
		Map<String, Object> response = new HashMap<>();
		Optional<Item> itemOptional = itemRepo.findById(itemId);

		if (itemOptional.isPresent()) {
			try {
				Item item = itemOptional.get();
				System.out.println("🔹 Deleting item with ID: " + itemId);

				// ✅ Remove related cart entries
				cartRepo.deleteAll(cartRepo.findByItem(item));

				// ✅ Remove related orders

				// ✅ Remove related wishlist entries
				wishlistRepo.deleteAll(wishlistRepo.findByItem(item));

				// ✅ Remove related item tags
				itemTagRepo.deleteAll(itemTagRepo.findByItem(item));

				// ✅ Remove related views
				viewRepo.deleteAll(viewRepo.findByItem(item));

				// ✅ Remove related approval records
				itemApprovalRepo.deleteByItem(item);

				// ✅ Remove related notifications
				notificationRepo.deleteAllByItem(item);

				// ✅ Remove related auction & auction track records
				Auction auction = auctionRepo.findByItem_ItemID(itemId);
				if (auction != null) {
					auctionTrackRepo.deleteAll(auctionTrackRepo.findByAuction(auction));
					auctionRepo.delete(auction);
				}

				// ✅ Remove item images from the filesystem
				deleteItemImages(item);

				// ✅ Delete the item itself
				itemRepo.delete(item);

				response.put("success", true);
				response.put("message", "✅ Item deleted successfully.");
			} catch (Exception e) {
				response.put("success", false);
				response.put("message", "❌ Error deleting item: " + e.getMessage());
			}
		} else {
			response.put("success", false);
			response.put("message", "❌ Item not found.");
		}

		return ResponseEntity.ok(response);
	}

	@GetMapping("/admin/orders")
	public String viewOrders(HttpSession session, Model model, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "6") int size) { // ✅ Set default page size

		if (session.getAttribute("admin") == null) { // ✅ Ensure admin is logged in
			return "redirect:/loginPage?error=Session Expired";
		}

		Pageable pageable = PageRequest.of(page, size);
		Page<Order> orderPage = orderRepo.findAll(pageable); // ✅ Fetch paginated orders

		model.addAttribute("orders", orderPage.getContent()); // ✅ List of orders
		model.addAttribute("currentPage", page); // ✅ Current page number
		model.addAttribute("totalPages", orderPage.getTotalPages()); // ✅ Total pages

		return "admin/orders"; // ✅ Render Thymeleaf template
	}

	@GetMapping("/admin/sellers")
	public String viewSellers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "6") int size,
			@RequestParam(required = false) String approval, Model model, HttpSession session) {
		if (session.getAttribute("admin") == null) { // ✅ Check if admin session exists
			return "redirect:/loginPage?error=Session Expired"; // ✅ Redirect to login if session expired
		}
		Pageable pageable = PageRequest.of(page, size);

		Page<Seller> sellerPage;

		if (approval != null && !approval.isEmpty()) {
			sellerPage = sellerRepo.findByApproval(approval, pageable);
		} else {
			sellerPage = sellerRepo.findAll(pageable);
		}

		model.addAttribute("sellerPage", sellerPage);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", sellerPage.getTotalPages());
		model.addAttribute("approval", approval);

		return "admin/sellers";
	}

	@GetMapping("/admin/users")
	public String viewUsers(Model model, @RequestParam(defaultValue = "0") int page, // Default to first page
			@RequestParam(defaultValue = "6") int size // Default page size of 6
			, HttpSession session) {
		if (session.getAttribute("admin") == null) { // ✅ Check if admin session exists
			return "redirect:/loginPage?error=Session Expired"; // ✅ Redirect to login if session expired
		}
		Pageable pageable = PageRequest.of(page, size);
		Page<User> userPage = userRepo.findAll(pageable);

		List<Map<String, Object>> userDetails = userPage.getContent().stream().map(user -> {
			Map<String, Object> userMap = new HashMap<>();
			userMap.put("userID", user.getUserID());
			userMap.put("username", user.getUsername());
			userMap.put("email", user.getEmail());
			userMap.put("phone", user.getPhone());
			userMap.put("dob", user.getDob());
			userMap.put("gender", user.getGender());
			userMap.put("bio", user.getBio());
			userMap.put("createdAt", user.getCreatedAt());

			// Determine Role: "BUYER", "SELLER", or "BOTH"
			if (user.getSeller() != null) {
				userMap.put("role", user.getSeller().getBusinessType().equalsIgnoreCase("C2C") ? "BOTH" : "SELLER");
			} else {
				userMap.put("role", "BUYER");
			}

			return userMap;
		}).toList();

		model.addAttribute("users", userDetails);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", userPage.getTotalPages());

		return "admin/users";
	}

	private void deleteItemImages(Item item) {
		List<ItemImage> images = itemImageRepo.findByItem(item);

		if (!images.isEmpty()) {
			for (ItemImage image : images) {
				Path imagePath = Paths.get(BASE_DIR + item.getItemID(), image.getImagePath());

				try {
					Files.deleteIfExists(imagePath); // ✅ Delete from filesystem
				} catch (IOException e) {
					System.err.println("❌ Failed to delete image file: " + imagePath);
					e.printStackTrace();
				}
			}
			itemImageRepo.deleteAll(images); // ✅ Delete from database
		}
	}

	@GetMapping("/admin/viewSeller/{sellerID}")
	public String viewSellerProfileAsAdmin(@PathVariable Long sellerID, Model model, HttpSession session) {
		// ✅ Ensure Admin is Logged In
		User admin = (User) session.getAttribute("user");

		// ✅ Fetch the Seller's Profile
		Optional<User> sellerOptional = userRepo.findById(sellerID);
		if (sellerOptional.isEmpty()) {
			return "redirect:/admin/dashboard?error=SellerNotFound";
		}
		User seller = sellerOptional.get();

		// ✅ Fetch only APPROVED items sold by the seller
		List<Item> itemList = itemRepo.findBySeller_UserIDAndApprove(seller.getUserID(), Item.ApprovalStatus.APPROVED);
		int postCount = itemList.size();

		// ✅ Separate auction items from normal items (only APPROVED ones)
		List<Item> auctionItemList = itemList.stream().filter(item -> item.getSellType() == Item.SellType.AUCTION)
				.toList();

		// ✅ Fetch auctions related to the seller's auction items
		List<Auction> auctionResults = auctionRepo.findAllByItemIn(auctionItemList);

		// ✅ Fetch highest bids for filtered auctions
		Map<Long, Double> auctionMaxBids = new HashMap<>();
		for (Auction auction : auctionResults) {
			Double maxBid = auctionTrackRepo.findMaxPriceByAuctionID(auction.getAuctionID());
			auctionMaxBids.put(auction.getAuctionID(), maxBid != null ? maxBid : auction.getStartPrice());
		}

		// ✅ Determine Role: "BUYER", "SELLER", or "BOTH"
		String businessType = "BUYER"; // Default
		if (seller.getSeller() != null) {
			businessType = seller.getSeller().getBusinessType(); // "C2C" or "B2C"
		}

		// ✅ Fetch Total Orders, Sales, Spending, and Visits
		int totalOrdersPlaced = orderRepo.countDistinctOrdersByBuyer(seller.getUserID()); // Orders placed as a BUYER
		int totalOrdersReceived = orderRepo.countOrdersBySeller(seller.getUserID()); // Orders received as a SELLER
		double totalSpending = orderRepo.sumTotalSpentByBuyer(seller.getUserID()); // Money spent as BUYER
		double totalSales = orderRepo.sumTotalSalesBySeller(seller.getUserID()); // Sales revenue as SELLER
		int totalVisits = viewRepo.countVisitsBySeller(seller.getUserID()); // Seller profile visits

		// ✅ Fetch reviews for the seller
		List<Review> reviews = reviewRepo.findByReviewed_UserID(seller.getUserID());
		int reviewCount = reviews.size();
		Double averageRating = reviewRepo.getAverageRating(seller.getUserID());

		// ✅ Add attributes to the model
		model.addAttribute("businessType", businessType);
		model.addAttribute("seller", seller);
		model.addAttribute("postCount", postCount);
		model.addAttribute("itemList", itemList);
		model.addAttribute("auctionItemList", auctionItemList);
		model.addAttribute("auctionResults", auctionResults);
		model.addAttribute("auctionMaxBids", auctionMaxBids);
		model.addAttribute("totalOrdersPlaced", totalOrdersPlaced);
		model.addAttribute("totalOrdersReceived", totalOrdersReceived);
		model.addAttribute("totalSpending", totalSpending);
		model.addAttribute("totalSales", totalSales);
		model.addAttribute("totalVisits", totalVisits);
		model.addAttribute("reviewCount", reviewCount);
		model.addAttribute("reviews", reviews);
		model.addAttribute("averageRating", averageRating != null ? averageRating : 0.0);

		return "admin/viewSellerProfile"; // ✅ Updated Admin Page
	}

	@GetMapping("/sellerStats")
	public Map<String, Object> getSellerStats(@RequestParam Long sellerID) {
		Map<String, Object> response = new HashMap<>();

		// Get total orders, sales, visits
		int totalOrders = orderRepo.countOrdersBySeller(sellerID);
		double totalSales = orderRepo.sumSalesBySeller(sellerID);
		int totalVisits = viewRepo.countVisitsBySeller(sellerID);

		// Get monthly trends
		List<Object[]> orders = orderRepo.findMonthlyOrdersBySeller(sellerID);
		List<Object[]> sales = orderRepo.findMonthlySalesBySeller(sellerID);
		List<Object[]> visits = viewRepo.findMonthlyViewsBySeller(sellerID);

		response.put("totalOrders", totalOrders);
		response.put("totalSales", totalSales);
		response.put("totalVisits", totalVisits);
		response.put("orders", formatStats(orders));
		response.put("sales", formatStats(sales));
		response.put("visits", formatStats(visits));

		return response;
	}

	private List<Map<String, Object>> formatStats(List<Object[]> stats) {
		return stats.stream().map(stat -> {
			Map<String, Object> data = new HashMap<>();
			data.put("month", stat[0]);
			data.put("value", stat[1]);
			return data;
		}).toList();
	}

	@GetMapping("/api/sellerStats")
	@ResponseBody
	public ResponseEntity<?> getSellerStatsAdmin(@RequestParam Long sellerID) {
		if (sellerID == null) {
			return ResponseEntity.badRequest().body(Map.of("error", "Seller ID is required"));
		}

		int totalOrders = orderRepo.countOrdersBySeller(sellerID);
		double totalSales = orderRepo.sumSalesBySeller(sellerID);
		int totalVisits = viewRepo.countVisitsBySeller(sellerID);

		return ResponseEntity
				.ok(Map.of("totalOrders", totalOrders, "totalSales", totalSales, "totalVisits", totalVisits));
	}

}
