package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.Item;
import com.example.demo.entity.Item.ApprovalStatus;
import com.example.demo.entity.ItemApproval;
import com.example.demo.entity.Notification;
import com.example.demo.entity.Seller;
import com.example.demo.entity.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ItemApprovalRepository;
import com.example.demo.repository.ItemImageRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.SellerRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.ViewRepository;
import com.example.demo.repository.Auction.AuctionRepository;
import com.example.demo.repository.Auction.AuctionTrackRepository;
import com.example.demo.repository.tag.ItemTagRepository;
import com.example.demo.repository.tag.TagRepository;

@Controller
public class AdminController {

	@Autowired
	ItemRepository itemRepo;

	@Autowired
	CategoryRepository categoryRepo;

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

	@GetMapping("/admin/viewDashboard")
	public String viewDashboard() {
		return "admin/dashboard";

	}

	// Approval
	@GetMapping("/admin/approvals")
	public String viewApprovals(Model model) {

		List<Item> pendingItems = itemRepo.findByApprove(ApprovalStatus.PENDING);
		model.addAttribute("pendingItems", pendingItems);
		List<Seller> pendingSellers = sellerRepo.findByApproval("pending");
		model.addAttribute("pendingSellers", pendingSellers);
		return "admin/approvals"; // ✅ Thymeleaf template for approvals

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
	public String viewAuctions() {
		return "admin/auctions";

	}

	@GetMapping("/admin/delivery")
	public String viewDelivery() {
		return "admin/delivery";

	}

	@GetMapping("/admin/items")
	public String viewItems() {
		return "admin/items";

	}

	@GetMapping("/admin/orders")
	public String viewOrders() {
		return "admin/orders";

	}

	@GetMapping("/admin/sellers")
	public String viewSellers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "6") int size,
			@RequestParam(required = false) String approval, Model model) {
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
	) {
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

}
