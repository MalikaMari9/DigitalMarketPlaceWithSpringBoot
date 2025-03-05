package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.User;
import com.example.demo.entity.Wishlist;
import com.example.demo.entity.Auction.Auction;
import com.example.demo.entity.Auction.AuctionTrack;
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
}
