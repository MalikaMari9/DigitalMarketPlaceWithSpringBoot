package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Item;
import com.example.demo.entity.Item.ApprovalStatus;

public interface ItemRepository extends JpaRepository<Item, Long> {

	@Query("SELECT t.name FROM Tag t " + "JOIN ItemTag it ON t.tagID = it.tag.tagID "
			+ "JOIN Item i ON i.itemID = it.item.itemID " + "WHERE i.itemID = :itemID")
	List<String> findTagNamesByItemId(@Param("itemID") Long itemID);

	// Searching item without category through tags, name and category name
	@Query("""
			    SELECT i
			    FROM Item i
			    LEFT JOIN FETCH i.category c
			    LEFT JOIN FETCH i.itemTags it
			    LEFT JOIN FETCH it.tag t
			    LEFT JOIN FETCH Auction a ON a.item = i
			    WHERE i.approve = 'APPROVED'
			    AND (
			        LOWER(i.itemName) LIKE LOWER(CONCAT('%', :query, '%'))
			        OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))
			        OR LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))
			    )
			""")
	List<Item> searchItems(@Param("query") String query);

	@Query("""
			    SELECT i
			    FROM Item i
			    LEFT JOIN FETCH i.category c
			    LEFT JOIN FETCH i.itemTags it
			    LEFT JOIN FETCH it.tag t
			    LEFT JOIN FETCH Auction a ON a.item = i
			    WHERE i.approve = 'APPROVED'
			    AND (
			        LOWER(i.itemName) LIKE LOWER(CONCAT('%', :query, '%'))
			        OR LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))
			    )
			    AND c.catID IN (
			        SELECT sub.catID FROM Category sub
			        WHERE sub.catID = :categoryID
			        OR sub.parentCategory.catID = :categoryID
			        OR sub.parentCategory.parentCategory.catID = :categoryID
			    )
			""")
	List<Item> searchItemsByCategory(@Param("query") String query, @Param("categoryID") Long categoryID);

	@Query("""
			    SELECT i
			    FROM Item i
			    LEFT JOIN FETCH i.category c
			    LEFT JOIN FETCH i.itemTags it
			    LEFT JOIN FETCH it.tag t
			    LEFT JOIN FETCH Auction a ON a.item = i
			    WHERE i.approve = 'APPROVED'
			    AND (
			        c.catID = :categoryID
			        OR c.catID IN (
			            SELECT sub.catID FROM Category sub
			            WHERE sub.parentCategory.catID = :categoryID
			            OR sub.parentCategory.parentCategory.catID = :categoryID
			        )
			    )
			""")
	List<Item> findItemsByCategory(@Param("categoryID") Long categoryID);

	@Query("""
			    SELECT i FROM Item i
			    WHERE i.approve = 'APPROVED'
			    AND i.seller.userID <> :userID
			    AND i.category.catID IN (
			        SELECT it.category.catID FROM View v
			        JOIN v.item it WHERE v.user.userID = :userID
			        GROUP BY it.category.catID ORDER BY COUNT(v) DESC
			    )
			""")
	List<Item> findRecommendedItemsByCategory(@Param("userID") Long userID);

	@Query("""
			    SELECT DISTINCT i FROM Item i
			    JOIN i.itemTags it
			    WHERE i.approve = 'APPROVED'
			    AND i.seller.userID <> :userID
			    AND it.tag.tagID IN (
			        SELECT t.tag.tagID FROM View v
			        JOIN v.item.itemTags t WHERE v.user.userID = :userID
			    )
			""")
	List<Item> findRecommendedItemsByTag(@Param("userID") Long userID);

	@Query("""
			    SELECT i FROM Item i
			    WHERE i.approve = 'APPROVED'
			    ORDER BY i.createdAt DESC
			""")
	Page<Item> findLatestItems(Pageable pageable);

	// Both Approved and non approved
	Page<Item> findBySeller_UserIDOrderByApproveDesc(Long sellerUserID, Pageable pageable);

	List<Item> findBySeller_UserID(Long sellerUserID);

	Page<Item> findBySeller_UserID(Long sellerUserID, Pageable pageable);

	List<Item> findByApprove(ApprovalStatus pending);

	@Query("""
			    SELECT i FROM Item i
			    LEFT JOIN FETCH i.itemApproval ia
			    WHERE i.seller.userID = :sellerID
			    AND (:searchText IS NULL OR LOWER(i.itemName) LIKE LOWER(CONCAT('%', :searchText, '%')))
			""")
	Page<Item> findPendingSales(@Param("sellerID") Long sellerID, @Param("searchText") String searchText,
			Pageable pageable // ✅ Pageable will now handle sorting
	);

	@Query("""
			    SELECT i FROM Item i
			    WHERE i.seller.userID = :sellerID
			    AND (:searchText IS NULL OR LOWER(i.itemName) LIKE LOWER(CONCAT('%', :searchText, '%')))
			    ORDER BY
			    CASE
			        WHEN :sortBy = 'itemID' THEN i.itemID
			        WHEN :sortBy = 'itemName' THEN i.itemName
			        WHEN :sortBy = 'price' THEN i.price
			    END ASC
			""")
	Page<Item> findWarehouseItems(@Param("sellerID") Long sellerID, @Param("searchText") String searchText,
			@Param("sortBy") String sortBy, Pageable pageable);

	int countByApprove(ApprovalStatus pending);

	List<Item> findBySeller_UserIDAndApprove(Long userID, ApprovalStatus approved);

}
