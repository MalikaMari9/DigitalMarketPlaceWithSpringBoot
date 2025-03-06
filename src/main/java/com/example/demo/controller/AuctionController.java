package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.entity.Wishlist;
import com.example.demo.entity.Auction.Auction;
import com.example.demo.entity.Auction.AuctionTrack;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.WishlistRepository;
import com.example.demo.repository.Auction.AuctionRepository;
import com.example.demo.repository.Auction.AuctionTrackRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuctionController {

	@Autowired
	private WishlistRepository wishlistRepo;

	@Autowired
	private AuctionTrackRepository auctionTrackRepo;
	@Autowired
	private AuctionRepository auctionRepo;
	@Autowired
	private ItemRepository itemRepo;
	@Autowired
	private NotificationRepository notificationRepo;

	// ✅ Watch Bid List (Shows only auction items from wishlist)
	@GetMapping("/watchlist")
	public String viewWatchlist(Model model, HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user == null) {
			return "redirect:/loginPage";
		}

		// ✅ Get all wishlisted items (including auctions)
		List<Wishlist> wishlistedItems = wishlistRepo.findByUser(user);

		// ✅ Filter to only include auction items
		List<Wishlist> watchlistItems = wishlistedItems.stream()
				.filter(wishlist -> wishlist.getItem().getSellType() == com.example.demo.entity.Item.SellType.AUCTION)
				.toList();

		// ✅ Fetch highest bid and auction end time for each auction item
		Map<Long, Double> auctionMaxBids = new HashMap<>();
		Map<Long, String> auctionEndTimes = new HashMap<>();

		for (Wishlist wishlist : watchlistItems) {
			Long itemId = wishlist.getItem().getItemID();

			// ✅ Get the corresponding auction for this item
			Auction auction = auctionRepo.findByItem(wishlist.getItem());

			if (auction != null) {
				// ✅ Fetch highest bid
				Double maxBid = auctionTrackRepo.findMaxPriceByAuctionID(auction.getAuctionID());
				auctionMaxBids.put(itemId, maxBid != null ? maxBid : auction.getStartPrice());

				// ✅ Fetch auction end time
				auctionEndTimes.put(itemId, auction.getRemainingTime());
			}
		}

		model.addAttribute("wishlistedItems", watchlistItems);
		model.addAttribute("auctionMaxBids", auctionMaxBids);
		model.addAttribute("auctionEndTimes", auctionEndTimes);

		return "watchBidList"; // ✅ Loads watchBidList.html
	}

	@GetMapping("/currentBidding")
	public String viewCurrentBidding(Model model, HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user == null) {
			return "redirect:/loginPage";
		}

		// ✅ Find all auctions where the user has placed bids
		List<AuctionTrack> userBids = auctionTrackRepo.findByUser(user);

		// ✅ Get unique auctions that are still active
		List<Auction> activeUserAuctions = userBids.stream().map(AuctionTrack::getAuction) // Get the auction from bid
				.filter(auction -> auction.getStat() == Auction.AuctionStatus.ACTIVE) // Only active auctions
				.distinct().collect(Collectors.toList());

		// ✅ Fetch highest bid & user's last bid for each auction
		Map<Long, Double> auctionMaxBids = new HashMap<>();
		Map<Long, Double> userLastBids = new HashMap<>();
		Map<Long, String> auctionEndTimes = new HashMap<>();

		for (Auction auction : activeUserAuctions) {
			Long auctionID = auction.getAuctionID();

			// ✅ Fetch highest bid
			Double maxBid = auctionTrackRepo.findMaxPriceByAuctionID(auctionID);
			auctionMaxBids.put(auctionID, maxBid != null ? maxBid : auction.getStartPrice());

			// ✅ Fetch user's last bid on this auction
			Double lastBid = auctionTrackRepo.findLastBidByUserAndAuction(user.getUserID(), auctionID);
			userLastBids.put(auctionID, lastBid != null ? lastBid : 0.0);

			// ✅ Fetch auction end time
			auctionEndTimes.put(auctionID, auction.getRemainingTime());
		}

		model.addAttribute("activeUserAuctions", activeUserAuctions);
		model.addAttribute("auctionMaxBids", auctionMaxBids);
		model.addAttribute("userLastBids", userLastBids);
		model.addAttribute("auctionEndTimes", auctionEndTimes);

		return "currentBidding"; // ✅ Load currentBidding.html
	}

	@GetMapping("/winningBids")
	public String viewWinningBids(Model model, HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user == null) {
			return "redirect:/loginPage";
		}

		// ✅ Find auctions where the user had the highest final bid
		List<AuctionTrack> userBids = auctionTrackRepo.findByUser(user);

		// ✅ Get auctions the user won (only COMPLETED auctions)
		List<Auction> wonAuctions = userBids.stream().map(AuctionTrack::getAuction) // Get auction
				.filter(auction -> auction.getStat() == Auction.AuctionStatus.COMPLETED) // Only completed auctions
				.filter(auction -> {
					Double maxBid = auctionTrackRepo.findMaxPriceByAuctionID(auction.getAuctionID());
					Double userBid = auctionTrackRepo.findLastBidByUserAndAuction(user.getUserID(),
							auction.getAuctionID());
					return maxBid != null && userBid != null && maxBid.equals(userBid);
				}).distinct().collect(Collectors.toList());

		// ✅ Fetch winning bid amount, payment status, and payment deadline
		Map<Long, Double> winningBids = new HashMap<>();
		Map<Long, Boolean> paymentStatus = new HashMap<>();
		Map<Long, LocalDateTime> paymentDeadlines = new HashMap<>();

		for (Auction auction : wonAuctions) {
			Long auctionID = auction.getAuctionID();

			// ✅ Get final winning bid (highest bid)
			Double winningBid = auctionTrackRepo.findMaxPriceByAuctionID(auctionID);
			winningBids.put(auctionID, winningBid != null ? winningBid : auction.getStartPrice());

			// ✅ Calculate payment deadline (48 hours after auction end)
			LocalDateTime paymentDeadline = auction.getEndTime().plusHours(72);
			paymentDeadlines.put(auctionID, paymentDeadline);

			// ✅ Check if the user has paid (assuming payment status is stored)
			boolean isPaid = checkPaymentStatus(user.getUserID(), auctionID); // Implement payment check logic
			paymentStatus.put(auctionID, isPaid);
		}

		model.addAttribute("wonAuctions", wonAuctions);
		model.addAttribute("winningBids", winningBids);
		model.addAttribute("paymentStatus", paymentStatus);
		model.addAttribute("paymentDeadlines", paymentDeadlines);

		return "winningBids"; // ✅ Load winningBids.html
	}

	@GetMapping("/auctionHistory")
	public String auctionHistory(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "6") int size,
			@RequestParam(required = false, defaultValue = "") String searchfield,
			@RequestParam(required = false, defaultValue = "item") String sortby, HttpSession session, Model model) {

		User seller = (User) session.getAttribute("user");
		if (seller == null) {
			return "redirect:/loginPage";
		}

		Sort sort = Sort.by(Sort.Direction.ASC, "item.itemName"); // Default sorting

		if ("price".equals(sortby)) {
			sort = Sort.by(Sort.Direction.ASC, "startPrice");
		} else if ("buyer".equals(sortby)) {
			sort = Sort.by(Sort.Direction.ASC, "item.itemID"); // ✅ Sort by item ID instead (avoids error)
		} else if ("Auctionstatus".equalsIgnoreCase(sortby)) {
			sort = Sort.by(Sort.Direction.ASC, "stat");
		}

		Pageable pageable = PageRequest.of(page, size, sort);

		// ✅ Fetch auctions based on search criteria
		Page<Auction> auctionPage;
		if (searchfield != null && !searchfield.isEmpty()) {
			auctionPage = auctionRepo.findBySellerAndSearch(seller, searchfield, pageable);
		} else {
			auctionPage = auctionRepo.findByItem_Seller(seller, pageable);
		}

		model.addAttribute("auctionPage", auctionPage);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", auctionPage.getTotalPages());
		model.addAttribute("searchfield", searchfield);
		model.addAttribute("sortby", sortby);

		return "auctionHistory";
	}

	@GetMapping("/updateAuctionStatus")
	public String updateAuctionStatuses() {
		List<Auction> expiredAuctions = auctionRepo.findExpiredAuctions(LocalDateTime.now());

		for (Auction auction : expiredAuctions) {
			User seller = auction.getItem().getSeller(); // Seller who owns the auction

			if (auction.hasNoBids()) {
				auction.setStat(Auction.AuctionStatus.FAILED);
				notificationRepo.save(new Notification(seller,
						"Your auction for '" + auction.getItem().getItemName() + "' ended with no bids.",
						"AUCTION_FAILED"));
			} else {
				auction.setStat(Auction.AuctionStatus.COMPLETED);
				User highestBidder = auction.getAuctionTracks().stream()
						.max(Comparator.comparing(AuctionTrack::getPrice)).get().getUser(); // Get the highest bidder

				notificationRepo.save(new Notification(seller,
						"Your auction for '" + auction.getItem().getItemName() + "' has ended. Winner: "
								+ highestBidder.getUsername() + " for USD $" + auction.getHighestBid(),
						"AUCTION_COMPLETED"));

				notificationRepo.save(
						new Notification(highestBidder, "You won the auction for '" + auction.getItem().getItemName()
								+ "'. Your winning bid: USD $" + auction.getHighestBid(), "AUCTION_WINNER"));
			}
			auctionRepo.save(auction);
		}
		return "redirect:/auctionHistory";
	}

	@PostMapping("/reactivate/{auctionID}")
	public ResponseEntity<String> reactivateAuction(@PathVariable Long auctionID, HttpSession session) {
		Auction auction = auctionRepo.findById(auctionID).orElse(null);

		if (auction == null) {
			return ResponseEntity.badRequest().body("❌ Auction not found.");
		}

		User loggedInUser = (User) session.getAttribute("user");

		// ✅ Ensure only the seller can reactivate the auction
		if (loggedInUser == null || !auction.getItem().getSeller().getUserID().equals(loggedInUser.getUserID())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body("❌ You are not authorized to reactivate this auction.");
		}

		// ✅ Ensure auction is actually FAILED before allowing reactivation
		if (auction.getStat() != Auction.AuctionStatus.FAILED) {
			return ResponseEntity.badRequest().body("❌ Only failed auctions can be reactivated.");
		}

		// ✅ Extend auction end date by 7 days
		auction.setEndTime(LocalDateTime.now().plusDays(7));
		auction.setStat(Auction.AuctionStatus.ACTIVE); // Change status to ACTIVE
		auctionRepo.save(auction);

		return ResponseEntity.ok("✅ Auction successfully reactivated for 7 more days!");
	}

	// ✅ Mock payment check (replace with actual payment status retrieval)
	private boolean checkPaymentStatus(Long userID, Long auctionID) {
		// Implement actual payment status check (e.g., from payment_tbl)
		return false; // Default: Not paid
	}
}
