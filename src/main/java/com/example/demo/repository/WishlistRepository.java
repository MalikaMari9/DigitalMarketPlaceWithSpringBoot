package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Item;
import com.example.demo.entity.User;
import com.example.demo.entity.Wishlist;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

	// ✅ Find all wishlist items for a specific user
	List<Wishlist> findByUser(User user);

	// ✅ Check if a user has already wishlisted an item
	Optional<Wishlist> findByUserAndItem(User user, Item item);

	// ✅ Remove an item from a user's wishlist
	void deleteByUserAndItem(User user, Item item);

	// ✅ Count the number of users who have wishlisted a specific item
	@Query("SELECT COUNT(w) FROM Wishlist w WHERE w.item = :item")
	int countWishlistByItem(@Param("item") Item item);

	// ✅ Find all users who have wishlisted a specific item
	@Query("SELECT w.user FROM Wishlist w WHERE w.item = :item")
	List<User> findUsersWhoWishlistedItem(@Param("item") Item item);

	@Query("SELECT w.item.itemID FROM Wishlist w WHERE w.user.userID = :userID")
	List<Long> findItemIdsByUser(@Param("userID") Long userID);

}
