package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Item;
import com.example.demo.entity.User;
import com.example.demo.entity.View;

@Repository
public interface ViewRepository extends JpaRepository<View, Long> {

	// ✅ Find all views by item
	List<View> findByItem(Item item);

	// ✅ Delete all views related to an item
	void deleteAllByItem(Item item);

	// ✅ Count views of an item
	@Query("SELECT COUNT(v) FROM View v WHERE v.item.itemID = :itemID")
	Long countViewsByItem(@Param("itemID") Long itemID);

	// ✅ Find most viewed categories by a user
	@Query("""
			    SELECT v.item.category.catID
			    FROM View v
			    WHERE v.user.userID = :userID
			    AND v.item.itemID NOT IN (SELECT c.item.itemID FROM Cart c WHERE c.user.userID = :userID)
			    GROUP BY v.item.category.catID
			    ORDER BY COUNT(v) DESC
			""")
	List<Long> findMostViewedCategoriesByUser(@Param("userID") Long userID);

	// ✅ Find most viewed tags by a user
	@Query("""
			    SELECT it.tag.tagID
			    FROM View v
			    JOIN v.item.itemTags it
			    WHERE v.user.userID = :userID
			    AND it.item.itemID NOT IN (SELECT c.item.itemID FROM Cart c WHERE c.user.userID = :userID)
			    GROUP BY it.tag.tagID
			    ORDER BY COUNT(it) DESC
			""")
	List<Long> findMostViewedTagsByUser(@Param("userID") Long userID);

	// ✅ Find items viewed by users who viewed the same item
	@Query("""
			    SELECT DISTINCT v2.item
			    FROM View v1
			    JOIN View v2 ON v1.user.userID = v2.user.userID
			    WHERE v1.item.itemID = :itemID
			    AND v1.user.userID != :userID
			    AND v2.item.itemID NOT IN (SELECT c.item.itemID FROM Cart c WHERE c.user.userID = :userID)
			""")
	List<Item> findItemsViewedBySimilarUsers(@Param("itemID") Long itemID, @Param("userID") Long userID);

	@Query("SELECT COUNT(v) > 0 FROM View v WHERE v.item = :item AND v.user = :user")
	boolean existsByItemAndUser(@Param("item") Item item, @Param("user") User user);

	@Query("SELECT COUNT(v) FROM View v WHERE v.item.seller = :seller")
	Long countViewsBySeller(@Param("seller") User seller);

	@Query("SELECT COUNT(v) FROM View v WHERE v.item.seller = :seller AND MONTH(v.viewedAt) = :month")
	Long getMonthlyViews(@Param("seller") User seller, @Param("month") int month);

	@Query("SELECT COUNT(v) FROM View v")
	Long countTotalViews();

	@Query("SELECT COUNT(v) FROM View v WHERE MONTH(v.viewedAt) = :month")
	Long getMonthlyViews(int month);

	@Query("SELECT COUNT(v) FROM View v WHERE v.item.seller.userID = :sellerID")
	int countVisitsBySeller(@Param("sellerID") Long sellerID);

	// ✅ Monthly Views for Seller's Items
	@Query("SELECT MONTH(v.viewedAt), COUNT(v) FROM View v WHERE v.item.seller.userID = :sellerID "
			+ "GROUP BY MONTH(v.viewedAt) ORDER BY MONTH(v.viewedAt)")
	List<Object[]> findMonthlyViewsBySeller(@Param("sellerID") Long sellerID);

	@Query("""
			    SELECT v.item FROM View v
			    WHERE v.item.quality > 0
			    GROUP BY v.item
			    ORDER BY COUNT(v.item) DESC
			""")
	Page<Item> findMostViewedItems(Pageable pageable);

}
