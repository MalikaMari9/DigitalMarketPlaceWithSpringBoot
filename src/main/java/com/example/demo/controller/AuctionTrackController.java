package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Cart;
import com.example.demo.entity.User;
import com.example.demo.entity.Auction.Auction;
import com.example.demo.entity.Auction.AuctionTrack;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WishlistRepository;
import com.example.demo.repository.Auction.AuctionRepository;
import com.example.demo.repository.Auction.AuctionTrackRepository;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/auction")
public class AuctionTrackController {

	@Autowired
	private AuctionTrackRepository auctionTrackRepository;

	@Autowired
	private AuctionRepository auctionRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private ItemRepository itemRepository;
	@Autowired
	private WishlistRepository wishlistRepo;

	@PostMapping("/placeBid")
	@Transactional
	public ResponseEntity<?> placeBid(@RequestBody Map<String, Object> payload) {
		try {
			Long auctionID = Long.valueOf(payload.get("auctionID").toString());
			Long userID = Long.valueOf(payload.get("userID").toString());
			Double bidAmount = Double.valueOf(payload.get("bidAmount").toString());

			System.out
					.println("📥 Received bid: AuctionID=" + auctionID + ", UserID=" + userID + ", Price=" + bidAmount);

			Optional<Auction> auctionOpt = auctionRepository.findById(auctionID);
			Optional<User> userOpt = userRepository.findById(userID);

			if (auctionOpt.isEmpty() || userOpt.isEmpty()) {
				return ResponseEntity.badRequest()
						.body(Map.of("success", false, "message", "Invalid auction or user."));
			}

			Auction auction = auctionOpt.get();
			User user = userOpt.get();

			// ✅ Fetch the current highest bid
			Optional<AuctionTrack> highestBidOpt = auctionTrackRepository.findTopByAuctionOrderByPriceDesc(auction);
			double highestBid = highestBidOpt.map(AuctionTrack::getPrice).orElse(auction.getStartPrice());

			if (bidAmount <= highestBid) {
				return ResponseEntity.badRequest().body(
						Map.of("success", false, "message", "Your bid must be higher than the current highest bid."));
			}

			// ✅ Insert a new bid (ALWAYS)
			AuctionTrack newBid = new AuctionTrack();
			newBid.setAuction(auction);
			newBid.setUser(user);
			newBid.setPrice(bidAmount);
			newBid.setCreatedAt(LocalDateTime.now());

			auctionTrackRepository.save(newBid);
			System.out.println("✅ New bid inserted: " + newBid.getPrice());

			return ResponseEntity.ok(Map.of("success", true, "message", "Bid placed successfully!"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500)
					.body(Map.of("success", false, "message", "Error processing bid: " + e.getMessage()));
		}
	}

	// ✅ Check expired auctions and move the highest bidder's item to their cart
	@PostMapping("/processExpiredAuctions")
	public String processExpiredAuctions() {
		System.out.println("🔍 Checking for expired auctions...");

		List<Auction> expiredAuctions = auctionRepository.findByEndTimeBeforeAndStat(LocalDateTime.now(),
				Auction.AuctionStatus.ACTIVE);

		if (expiredAuctions.isEmpty()) {
			System.out.println("✅ No expired auctions found.");
			return "No expired auctions found.";
		}

		for (Auction auction : expiredAuctions) {
			System.out.println("📌 Processing auction ID: " + auction.getAuctionID());

			Optional<AuctionTrack> highestBid = auctionTrackRepository.findTopByAuctionOrderByPriceDesc(auction);
			if (highestBid.isPresent()) {
				AuctionTrack winningBid = highestBid.get();
				User winner = winningBid.getUser();

				System.out.println(
						"🏆 Highest bidder ID: " + winner.getUserID() + " | Bid Price: " + winningBid.getPrice());

				// ✅ Calculate payment deadline dynamically (48 hours from auction end)
				LocalDateTime paymentDeadline = auction.getEndTime().plusHours(72);
				System.out.println("⏳ Payment deadline for User ID " + winner.getUserID() + ": " + paymentDeadline);

				// ✅ Add item to the winner's cart
				Cart cart = new Cart();
				cart.setItem(auction.getItem());
				cart.setUser(winner);
				cart.setQuantity(1);
				cart.setCreatedAt(LocalDateTime.now());

				cartRepository.save(cart);
				System.out.println("✅ Item added to cart for User ID: " + winner.getUserID());

				// ✅ Mark auction as completed
				auction.setStat(Auction.AuctionStatus.COMPLETED);
				auctionRepository.save(auction);
				System.out.println("✅ Auction ID " + auction.getAuctionID() + " marked as COMPLETED.");
			} else {
				System.out.println("⚠️ No bids found for auction ID: " + auction.getAuctionID());
			}
		}
		return "Processed expired auctions successfully.";
	}

	@GetMapping("/getHighestBid")
	public ResponseEntity<?> getHighestBid(@RequestParam Long auctionID) {
		Optional<Auction> auctionOpt = auctionRepository.findById(auctionID);

		if (auctionOpt.isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid auction ID."));
		}

		Auction auction = auctionOpt.get();

		Optional<AuctionTrack> highestBid = auctionTrackRepository.findTopByAuctionOrderByPriceDesc(auction);

		if (highestBid.isPresent()) {
			return ResponseEntity.ok(Map.of("success", true, "highestBid", highestBid.get().getPrice()));
		} else {
			return ResponseEntity.ok(Map.of("success", true, "highestBid", auction.getStartPrice())); // If no bids,
																										// return start
																										// price
		}
	}

}
