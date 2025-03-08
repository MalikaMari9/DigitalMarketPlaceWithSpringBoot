package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Item;

import jakarta.transaction.Transactional;

@Repository
public interface ItemDeleteRepository extends JpaRepository<Item, Long> {

	// ✅ Step 1: Delete Auction Track records before deleting Auctions
	@Transactional
	@Modifying
	@Query("DELETE FROM AuctionTrack at WHERE at.auction.id IN (SELECT a.id FROM Auction a WHERE a.item.id = :itemId)")
	void deleteAuctionTracksByItemId(@Param("itemId") Long itemId);

	// ✅ Step 2: Delete Auctions after deleting Auction Tracks
	@Transactional
	@Modifying
	@Query("DELETE FROM Auction a WHERE a.item.id = :itemId")
	void deleteAuctionsByItemId(@Param("itemId") Long itemId);

	// ✅ Step 3: Delete Other Dependencies
	@Transactional
	@Modifying
	@Query("DELETE FROM ItemApproval ia WHERE ia.item.id = :itemId")
	void deleteItemApprovalsByItemId(@Param("itemId") Long itemId);

	@Transactional
	@Modifying
	@Query("DELETE FROM Order o WHERE o.item.id = :itemId")
	void deleteOrdersByItemId(@Param("itemId") Long itemId);

	@Transactional
	@Modifying
	@Query("DELETE FROM Cart c WHERE c.item.id = :itemId")
	void deleteCartsByItemId(@Param("itemId") Long itemId);

	@Transactional
	@Modifying
	@Query("DELETE FROM Wishlist w WHERE w.item.id = :itemId")
	void deleteWishlistByItemId(@Param("itemId") Long itemId);

	@Transactional
	@Modifying
	@Query("DELETE FROM ItemImage i WHERE i.item.id = :itemId")
	void deleteItemImagesByItemId(@Param("itemId") Long itemId);

	@Transactional
	@Modifying
	@Query("DELETE FROM Notification n WHERE n.item.id = :itemId")
	void deleteNotificationsByItemId(@Param("itemId") Long itemId);

	@Transactional
	@Modifying
	@Query("DELETE FROM View v WHERE v.item.id = :itemId")
	void deleteViewsByItemId(@Param("itemId") Long itemId);

	@Transactional
	@Modifying
	@Query("DELETE FROM ItemTag it WHERE it.item.id = :itemId")
	void deleteItemTagsByItemId(@Param("itemId") Long itemId);

	// ✅ Step 4: Finally, delete the Item
	@Transactional
	@Modifying
	@Query("DELETE FROM Item i WHERE i.id = :itemId")
	void deleteItemById(@Param("itemId") Long itemId);
}
