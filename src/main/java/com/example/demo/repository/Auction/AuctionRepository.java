package com.example.demo.repository.Auction;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Item;
import com.example.demo.entity.Auction.Auction;

public interface AuctionRepository extends JpaRepository<Auction, Long> {

	Auction findByItem_ItemID(Long itemID);

	@Query("SELECT a FROM Auction a WHERE a.item IN :items")
	List<Auction> findAllByItemIn(@Param("items") List<Item> items);

	List<Auction> findByEndTimeBeforeAndStat(LocalDateTime now, Auction.AuctionStatus status);

	Auction findByItem(Item item);

	List<Auction> findByItemIn(List<Item> userItems);

	// Auctions where the logged-in user is the highest bidder
	@Query("SELECT a FROM Auction a WHERE a.auctionID IN " + "(SELECT at.auction.auctionID FROM AuctionTrack at "
			+ "WHERE at.user.userID = :userID "
			+ "AND at.price = (SELECT MAX(at2.price) FROM AuctionTrack at2 WHERE at2.auction.auctionID = at.auction.auctionID))")
	List<Auction> findByHighestBidderUserID(@Param("userID") Long userID);

	@Query("SELECT a FROM Auction a WHERE a.item.seller.userID = :userID")
	List<Auction> findBySellerUserID(@Param("userID") Long userID);

	@Query("SELECT a FROM Auction a LEFT JOIN FETCH a.auctionTracks WHERE a.endTime <= :now AND a.stat = 'ACTIVE'")
	List<Auction> findExpiredAuctions(@Param("now") LocalDateTime now);

}