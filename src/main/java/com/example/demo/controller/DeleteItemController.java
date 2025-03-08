package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.repository.ItemDeleteRepository;

@RestController
@RequestMapping("/admin/item-management")
public class DeleteItemController {

	@Autowired
	private ItemDeleteRepository itemRepo;

	@Transactional
	@DeleteMapping("/delete/{itemId}")
	public ResponseEntity<?> deleteItem(@PathVariable Long itemId) {
		try {
			// **Delete related records first**
			itemRepo.deleteOrdersByItemId(itemId);
			itemRepo.deleteAuctionsByItemId(itemId);
			itemRepo.deleteCartsByItemId(itemId);
			itemRepo.deleteWishlistByItemId(itemId);
			itemRepo.deleteItemImagesByItemId(itemId);
			itemRepo.deleteItemTagsByItemId(itemId);
			itemRepo.deleteNotificationsByItemId(itemId);
			itemRepo.deleteViewsByItemId(itemId);
			itemRepo.deleteItemApprovalsByItemId(itemId); // **Newly added**

			// **Finally, delete the item itself**
			itemRepo.deleteItemById(itemId);

			return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Item deleted successfully!\"}");
		} catch (Exception e) {
			e.printStackTrace(); // Log for debugging
			return ResponseEntity.status(500)
					.body("{\"success\": false, \"message\": \"Error deleting item: " + e.getMessage() + "\"}");
		}
	}

}
