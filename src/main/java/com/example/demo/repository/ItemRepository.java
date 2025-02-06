package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {

	@Query("SELECT t.name FROM Tag t " + "JOIN ItemTag it ON t.tagID = it.tag.tagID "
			+ "JOIN Item i ON i.itemID = it.item.itemID " + "WHERE i.itemID = :itemID")
	List<String> findTagNamesByItemId(@Param("itemID") Long itemID);

	@Query("SELECT i FROM Item i " + "LEFT JOIN FETCH i.category c " + "LEFT JOIN FETCH i.itemTags it "
			+ "LEFT JOIN FETCH it.tag t " + "LEFT JOIN FETCH Auction a ON a.item = i " + // ✅ Fetch auction details
			"WHERE LOWER(i.itemName) LIKE LOWER(CONCAT('%', :query, '%')) "
			+ "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) "
			+ "OR LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))")
	List<Item> searchItems(@Param("query") String query);

	@Query("SELECT i FROM Item i " + "LEFT JOIN FETCH i.category c " + "LEFT JOIN FETCH i.itemTags it "
			+ "LEFT JOIN FETCH it.tag t " + "LEFT JOIN FETCH Auction a ON a.item = i "
			+ "WHERE (LOWER(i.itemName) LIKE LOWER(CONCAT('%', :query, '%')) "
			+ "OR LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))) "
			+ "AND c.catID IN (SELECT sub.catID FROM Category sub WHERE sub.catID = :categoryID OR sub.parentCategory.catID = :categoryID OR sub.parentCategory.parentCategory.catID = :categoryID)")
	List<Item> searchItemsByCategory(@Param("query") String query, @Param("categoryID") Long categoryID);

	@Query("SELECT i FROM Item i " + "LEFT JOIN FETCH i.category c " + "LEFT JOIN FETCH i.itemTags it "
			+ "LEFT JOIN FETCH it.tag t " + "LEFT JOIN FETCH Auction a ON a.item = i "
			+ "WHERE c.catID IN (SELECT sub.catID FROM Category sub WHERE sub.catID = :categoryID OR sub.parentCategory.catID = :categoryID OR sub.parentCategory.parentCategory.catID = :categoryID)")
	List<Item> findItemsByCategory(@Param("categoryID") Long categoryID);

}
