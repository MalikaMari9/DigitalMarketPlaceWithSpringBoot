package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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

import com.example.demo.entity.Item;
import com.example.demo.entity.Item.ApprovalStatus;
import com.example.demo.entity.ItemApproval;
import com.example.demo.entity.ItemImage;
import com.example.demo.entity.Notification;
import com.example.demo.entity.Order;
import com.example.demo.entity.Seller;
import com.example.demo.entity.User;
import com.example.demo.entity.Auction.Auction;
import com.example.demo.entity.Auction.AuctionTrack;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ItemApprovalRepository;
import com.example.demo.repository.ItemImageRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.OrderRepository;
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

	private final String BASE_DIR = "src/main/resources/static/Image/Item/";

	@GetMapping("/admin/viewDashboard")
	public String viewDashboard() {
		return "admin/dashboard";

	}

	// Approval
	@GetMapping("/admin/approvals")
	public String viewApprovals(Model model, HttpSession session) {
		if (session.getAttribute("admin") == null) { // ✅ Check if admin session exists
			return "redirect:/loginPage?error=Session Expired"; // ✅ Redirect to login if session expired
		}

		List<Item> pendingItems = itemRepo.findByApprove(ApprovalStatus.PENDING);
		model.addAttribute("pendingItems", pendingItems);
		List<Seller> pendingSellers = sellerRepo.findByApproval("pending");
		model.addAttribute("pendingSellers", pendingSellers);
		return "admin/approvals"; // ✅ Thymeleaf template for approvals

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
	public String viewAuctions(Model model, HttpSession session) {
		if (session.getAttribute("admin") == null) { // ✅ Check if admin session exists
			return "redirect:/loginPage?error=Session Expired"; // ✅ Redirect to login if session expired
		}
		List<Auction> auctions = auctionRepo.findAll();
		model.addAttribute("auctions", auctions);
		return "admin/auctions";
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
			trackInfo.put("bidderName", track.getUser().getUsername());
			trackInfo.put("bidAmount", track.getPrice());
			trackInfo.put("time", track.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

			trackList.add(trackInfo);
		}

		return ResponseEntity.ok(trackList);
	}

	@GetMapping("/admin/delivery")
	public String viewDelivery(HttpSession session) {
		if (session.getAttribute("admin") == null) { // ✅ Check if admin session exists
			return "redirect:/loginPage?error=Session Expired"; // ✅ Redirect to login if session expired
		}
		return "admin/delivery";

	}

	@GetMapping("/admin/announcement")
	public String viewAnnouncement(HttpSession session) {
		if (session.getAttribute("admin") == null) { // ✅ Check if admin session exists
			return "redirect:/loginPage?error=Session Expired"; // ✅ Redirect to login if session expired
		}
		return "admin/announcement";

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
	public String viewOrders(HttpSession session, Model model) {
		if (session.getAttribute("admin") == null) { // ✅ Check if admin session exists
			return "redirect:/loginPage?error=Session Expired"; // ✅ Redirect to login if session expired
		}

		// ✅ Fetch all orders for admin
		List<Order> orders = orderRepo.findAll();
		model.addAttribute("orders", orders);

		return "admin/orders";
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
}
