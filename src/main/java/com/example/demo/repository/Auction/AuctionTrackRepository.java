package com.example.demo.repository.Auction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Auction.AuctionTrack;

public interface AuctionTrackRepository extends JpaRepository<AuctionTrack, Long> {

	@Query("SELECT MAX(at.price) FROM AuctionTrack at WHERE at.auction.auctionID = :auctionID")
	Double findMaxPriceByAuctionID(@Param("auctionID") Long auctionID);

	int countByAuction_AuctionID(Long auctionID);
}