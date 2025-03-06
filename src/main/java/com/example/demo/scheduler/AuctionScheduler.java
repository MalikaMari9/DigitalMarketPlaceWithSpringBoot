package com.example.demo.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.entity.Auction.Auction;
import com.example.demo.repository.Auction.AuctionRepository;

@Component
public class AuctionScheduler {

	@Autowired
	private AuctionRepository auctionRepository;

	@Scheduled(fixedRate = 100000) // Runs every 1 minute
	public void checkAndUpdateAuctionStatuses() {
		List<Auction> expiredAuctions = auctionRepository.findExpiredAuctions(LocalDateTime.now());

		for (Auction auction : expiredAuctions) {
			// ✅ Manually load auctionTracks before checking for bids
			Hibernate.initialize(auction.getAuctionTracks());

			if (auction.hasNoBids()) {
				auction.setStat(Auction.AuctionStatus.FAILED);
			} else {
				auction.setStat(Auction.AuctionStatus.COMPLETED);
			}
			auctionRepository.save(auction);
		}
		System.out.println("✅ Auction statuses updated at " + LocalDateTime.now());
	}

}
