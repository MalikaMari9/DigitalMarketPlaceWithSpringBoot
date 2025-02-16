package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.example.demo.entity.Seller;
import com.example.demo.entity.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ItemApprovalRepository;
import com.example.demo.repository.ItemImageRepository;
import com.example.demo.repository.ItemRepository;
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
		}

		sellerRepo.save(seller);
		return ResponseEntity.ok("✅ Seller Approved!");
	}

	// ✅ Reject Seller with Reason
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
	public String viewSellers() {
		return "admin/sellers";

	}

	@GetMapping("/admin/users")
	public String viewUsers() {
		return "admin/users";

	}

}
