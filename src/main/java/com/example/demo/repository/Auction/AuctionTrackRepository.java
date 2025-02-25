package com.example.demo.repository.Auction;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.User;
import com.example.demo.entity.Auction.Auction;
import com.example.demo.entity.Auction.AuctionTrack;

public interface AuctionTrackRepository extends JpaRepository<AuctionTrack, Long> {

	@Query("SELECT MAX(at.price) FROM AuctionTrack at WHERE at.auction.auctionID = :auctionID")
	Double findMaxPriceByAuctionID(@Param("auctionID") Long auctionID);

	int countByAuction_AuctionID(Long auctionID);

	List<AuctionTrack> findByAuction(Auction auction);

	// ✅ Fetch the LATEST bid of a user in a specific auction (Prevent multiple
	// results issue)
	@Query("SELECT a FROM AuctionTrack a WHERE a.auction.auctionID = :auctionID AND a.user.userID = :userID ORDER BY a.createdAt DESC")
	List<AuctionTrack> findLatestBidByAuctionAndUser(@Param("auctionID") Long auctionID, @Param("userID") Long userID);

	@Query("SELECT a FROM AuctionTrack a WHERE a.auction = :auction ORDER BY a.price DESC LIMIT 1")
	Optional<AuctionTrack> findTopByAuctionOrderByPriceDesc(@Param("auction") Auction auction);

	// ✅ Find the latest bid of a user in a specific auction
	Optional<AuctionTrack> findTopByAuctionAndUserOrderByCreatedAtDesc(Auction auction, User user);

	List<AuctionTrack> findByAuctionOrderByCreatedAtDesc(Auction auction);

}