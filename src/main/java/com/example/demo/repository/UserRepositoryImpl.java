package com.example.demo.repository;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class UserRepositoryImpl {

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public void deleteUserDependencies(Long userID) {

		// 1️⃣ Delete views related to the user's items
		entityManager
				.createNativeQuery(
						"DELETE FROM view_tbl WHERE itemid IN (SELECT itemid FROM item_tbl WHERE seller_userID = ?)")
				.setParameter(1, userID).executeUpdate();

		// 2️⃣ Delete views related to the user
		entityManager.createNativeQuery("DELETE FROM view_tbl WHERE userid = ?").setParameter(1, userID)
				.executeUpdate();

		// 3️⃣ **Delete auction bids before auctions**
		entityManager.createNativeQuery(
				"DELETE FROM auctiontrack_tbl WHERE auctionid IN (SELECT auctionid FROM auction_tbl WHERE itemid IN (SELECT itemid FROM item_tbl WHERE seller_userID = ?))")
				.setParameter(1, userID).executeUpdate();

		// 4️⃣ **Delete auctions after auction bids**
		entityManager
				.createNativeQuery(
						"DELETE FROM auction_tbl WHERE itemid IN (SELECT itemid FROM item_tbl WHERE seller_userID = ?)")
				.setParameter(1, userID).executeUpdate();

		// 3️⃣ **Delete delivery tracking records before deliveries**
		entityManager.createNativeQuery(
				"DELETE FROM deli_track_tbl WHERE deliid IN (SELECT deliid FROM delivery_tbl WHERE receiptid IN (SELECT receiptid FROM receipt_tbl WHERE userid_buyer = ? OR userid_seller = ?))")
				.setParameter(1, userID).setParameter(2, userID).executeUpdate();

		// 4️⃣ Delete payments before receipts
		entityManager.createNativeQuery(
				"DELETE FROM payment_tbl WHERE receiptid IN (SELECT receiptid FROM receipt_tbl WHERE userid_buyer = ? OR userid_seller = ?)")
				.setParameter(1, userID).setParameter(2, userID).executeUpdate();

		// 5️⃣ Delete orders before deleting receipts
		entityManager.createNativeQuery(
				"DELETE FROM order_tbl WHERE receiptid IN (SELECT receiptid FROM receipt_tbl WHERE userid_buyer = ? OR userid_seller = ?)")
				.setParameter(1, userID).setParameter(2, userID).executeUpdate();

		// 4️⃣ **Delete notifications related to receipts before deleting receipts**
		entityManager.createNativeQuery(
				"DELETE FROM notification_tbl WHERE receiptid IN (SELECT receiptid FROM receipt_tbl WHERE userid_buyer = ? OR userid_seller = ?)")
				.setParameter(1, userID).setParameter(2, userID).executeUpdate();

		// 12️⃣ Delete notifications
		entityManager.createNativeQuery("DELETE FROM notification_tbl WHERE sender_userid = ? OR userid = ?")
				.setParameter(1, userID).setParameter(2, userID).executeUpdate();

		// 5️⃣ **Delete deliveries before deleting receipts**
		entityManager.createNativeQuery(
				"DELETE FROM delivery_tbl WHERE receiptid IN (SELECT receiptid FROM receipt_tbl WHERE userid_buyer = ? OR userid_seller = ?)")
				.setParameter(1, userID).setParameter(2, userID).executeUpdate();

		// 6️⃣ **Delete receipts after dependent records**
		entityManager.createNativeQuery("DELETE FROM receipt_tbl WHERE userid_buyer = ? OR userid_seller = ?")
				.setParameter(1, userID).setParameter(2, userID).executeUpdate();

		// 7️⃣ Delete addresses (Fix for `address_tbl` foreign key issue)
		entityManager.createNativeQuery("DELETE FROM address_tbl WHERE userid = ?").setParameter(1, userID)
				.executeUpdate();

		// 8️⃣ Delete item approvals for the user's items
		entityManager.createNativeQuery(
				"DELETE FROM item_approval_tbl WHERE itemid IN (SELECT itemid FROM item_tbl WHERE seller_userID = ?)")
				.setParameter(1, userID).executeUpdate();

		// 9️⃣ Delete orders related to the user's items
		entityManager
				.createNativeQuery(
						"DELETE FROM order_tbl WHERE itemid IN (SELECT itemid FROM item_tbl WHERE seller_userID = ?)")
				.setParameter(1, userID).executeUpdate();

		// 🔟 Delete auctions related to the user's items
		entityManager
				.createNativeQuery(
						"DELETE FROM auction_tbl WHERE itemid IN (SELECT itemid FROM item_tbl WHERE seller_userID = ?)")
				.setParameter(1, userID).executeUpdate();

		// 🔟 Delete auction bids
		entityManager.createNativeQuery("DELETE FROM auctiontrack_tbl WHERE userid = ?").setParameter(1, userID)
				.executeUpdate();

		// 11️⃣ Delete carts containing the user's items
		entityManager
				.createNativeQuery(
						"DELETE FROM cart_tbl WHERE itemid IN (SELECT itemid FROM item_tbl WHERE seller_userID = ?)")
				.setParameter(1, userID).executeUpdate();

		// 9️⃣ Delete cart items
		entityManager.createNativeQuery("DELETE FROM cart_tbl WHERE userid = ?").setParameter(1, userID)
				.executeUpdate();

		// 12️⃣ Delete wishlist records for the user's items
		entityManager.createNativeQuery(
				"DELETE FROM wishlist_tbl WHERE itemid IN (SELECT itemid FROM item_tbl WHERE seller_userID = ?)")
				.setParameter(1, userID).executeUpdate();

		// 8️⃣ Delete wishlist items
		entityManager.createNativeQuery("DELETE FROM wishlist_tbl WHERE userid = ?").setParameter(1, userID)
				.executeUpdate();

		// 13️⃣ Delete images related to the user's items
		entityManager.createNativeQuery(
				"DELETE FROM item_images_tbl WHERE itemid IN (SELECT itemid FROM item_tbl WHERE seller_userID = ?)")
				.setParameter(1, userID).executeUpdate();

		// 14️⃣ Delete notifications related to the user's items
		entityManager.createNativeQuery(
				"DELETE FROM notification_tbl WHERE itemid IN (SELECT itemid FROM item_tbl WHERE seller_userID = ?)")
				.setParameter(1, userID).executeUpdate();

		// 15️⃣ Delete item tags for the user's items
		entityManager.createNativeQuery(
				"DELETE FROM itemtag_tbl WHERE item_tbl_itemid IN (SELECT itemid FROM item_tbl WHERE seller_userID = ?)")
				.setParameter(1, userID).executeUpdate();

		// 16️⃣ Delete user's listed items
		entityManager.createNativeQuery("DELETE FROM item_tbl WHERE seller_userID = ?").setParameter(1, userID)
				.executeUpdate();

		// 17️⃣ Delete messages where the user is sender or receiver
		entityManager.createNativeQuery("DELETE FROM message_tbl WHERE senderid = ? OR receiverid = ?")
				.setParameter(1, userID).setParameter(2, userID).executeUpdate();

		// 18️⃣ Delete seller records (if user is a seller)
		entityManager.createNativeQuery("DELETE FROM seller_tbl WHERE userid = ?").setParameter(1, userID)
				.executeUpdate();

		// 19️⃣ Delete reviews where user is involved
		entityManager.createNativeQuery("DELETE FROM review_tbl WHERE reviewerid = ? OR reviewedid = ?")
				.setParameter(1, userID).setParameter(2, userID).executeUpdate();

		// 20️⃣ Delete user's credit cards
		entityManager.createNativeQuery("DELETE FROM creditcard_tbl WHERE userid = ?").setParameter(1, userID)
				.executeUpdate();

		// 21️⃣ Delete password resets
		entityManager.createNativeQuery("DELETE FROM password_resets_tbl WHERE user_id = ?").setParameter(1, userID)
				.executeUpdate();

		// 22️⃣ **Finally, delete the user**
		entityManager.createNativeQuery("DELETE FROM user_tbl WHERE userid = ?").setParameter(1, userID)
				.executeUpdate();
	}
}
