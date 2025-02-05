package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Item;


public interface ItemRepository extends JpaRepository<Item, Long>{
	
	@Query("SELECT t.name FROM Tag t " +
	           "JOIN ItemTag it ON t.tagID = it.tag.tagID " +
	           "JOIN Item i ON i.itemID = it.item.itemID " +
	           "WHERE i.itemID = :itemID")
	    List<String> findTagNamesByItemId(@Param("itemID") Long itemID);
	
}
