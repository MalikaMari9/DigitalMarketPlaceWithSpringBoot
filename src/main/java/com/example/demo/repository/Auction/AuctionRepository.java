package com.example.demo.repository.Auction;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Item;
import com.example.demo.entity.User;
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

	Page<Auction> findByItem_Seller(User seller, Pageable pageable);

	@Query("SELECT a FROM Auction a JOIN a.item i WHERE i.seller = :seller AND LOWER(i.itemName) LIKE LOWER(CONCAT('%', :itemName, '%'))")
	Page<Auction> findByItem_SellerAndItem_ItemNameContainingIgnoreCase(@Param("seller") User seller,
			@Param("itemName") String itemName, Pageable pageable);

	@Query("SELECT a FROM Auction a WHERE a.item.seller = :seller AND (:searchfield IS NULL OR LOWER(a.item.itemName) LIKE LOWER(CONCAT('%', :searchfield, '%')) OR LOWER(a.stat) LIKE LOWER(CONCAT('%', :searchfield, '%')))")
	Page<Auction> findBySellerAndSearch(@Param("seller") User seller, @Param("searchfield") String searchfield,
			Pageable pageable);

	@Query("SELECT a FROM Auction a WHERE a.item IN:items ORDER	BY CASE	WHEN a.endTime>	CURRENT_TIMESTAMP AND a.item.quality>0 THEN 1	WHEN a.endTime<=CURRENT_TIMESTAMP AND a.item.quality>0 THEN 2 END,a.endTime DESC")

	List<Auction> findAllSortedByItemIn(@Param("items") List<Item> items);

}