package com.example.demo.scheduler;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.entity.Item;
import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.entity.Auction.Auction;
import com.example.demo.entity.Auction.AuctionTrack;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.Auction.AuctionRepository;

@Component
public class AuctionScheduler {

	@Autowired
	private AuctionRepository auctionRepository;

	@Autowired
	private NotificationRepository notificationRepo;

	@Scheduled(fixedRate = 60000) // ✅ Runs every 1 minute
	public void checkAndUpdateAuctionStatuses() {
		List<Auction> expiredAuctions = auctionRepository.findExpiredAuctions(LocalDateTime.now());

		for (Auction auction : expiredAuctions) {
			User seller = auction.getItem().getSeller();
			Item item = auction.getItem();

			if (auction.hasNoBids()) {
				auction.setStat(Auction.AuctionStatus.FAILED);
				notificationRepo.save(new Notification(seller, item, // ✅ Store item reference
						"Your auction for '" + item.getItemName() + "' ended with no bids.", "AUCTION_FAILED"));
			} else {
				auction.setStat(Auction.AuctionStatus.COMPLETED);
				User highestBidder = auction.getAuctionTracks().stream()
						.max(Comparator.comparing(AuctionTrack::getPrice)).get().getUser();

				notificationRepo.save(new Notification(seller, item, // ✅ Store item reference
						"Your auction for '" + item.getItemName() + "' has ended. Winner: "
								+ highestBidder.getUsername() + " for USD $" + auction.getHighestBid(),
						"AUCTION_COMPLETED"));

				notificationRepo.save(new Notification(highestBidder, item, // ✅ Store item reference
						"You won the auction for '" + item.getItemName() + "'. Your winning bid: USD $"
								+ auction.getHighestBid(),
						"AUCTION_WINNER"));

			}
			auctionRepository.save(auction);

		}
		System.out.println("✅ Auction statuses updated at " + LocalDateTime.now());
	}
}
