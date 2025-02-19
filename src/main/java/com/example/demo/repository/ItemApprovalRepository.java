package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Item;
import com.example.demo.entity.ItemApproval;

public interface ItemApprovalRepository extends JpaRepository<ItemApproval, Long> {

	// ✅ Find approval record by item
	ItemApproval findByItem(Item item);

	// ✅ Delete approval record by item
	void deleteByItem(Item item);

}