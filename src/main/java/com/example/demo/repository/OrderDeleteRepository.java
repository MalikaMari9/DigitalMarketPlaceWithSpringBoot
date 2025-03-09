package com.example.demo.repository;

import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;

@Repository
public class OrderDeleteRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public boolean deleteOrderByReceiptID(Long receiptID) {
        System.out.println("🔍 Step 1: Checking if receipt exists...");
        List<Long> receiptResult = entityManager
                .createNativeQuery("SELECT receiptid FROM receipt_tbl WHERE receiptid = ?")
                .setParameter(1, receiptID)
                .getResultList();

        if (receiptResult.isEmpty()) {
            System.out.println("❌ Receipt not found. Skipping deletion.");
            return false; 
        }

        System.out.println("✅ Found receipt ID: " + receiptID);

        // Fetch Order IDs related to the receipt
        System.out.println("🔍 Step 2: Fetching order IDs...");
        List<Long> orderIDs = entityManager
                .createNativeQuery("SELECT orderid FROM order_tbl WHERE receiptid = ?")
                .setParameter(1, receiptID)
                .getResultList();

        if (!orderIDs.isEmpty()) {
            System.out.println("✅ Found " + orderIDs.size() + " order(s) linked to receipt.");
            for (Long orderID : orderIDs) {
                System.out.println("🗑️ Deleting order ID: " + orderID);
                entityManager.createNativeQuery("DELETE FROM order_tbl WHERE orderid = ?")
                        .setParameter(1, orderID)
                        .executeUpdate();
                entityManager.flush();
            }
            System.out.println("✅ Orders deleted successfully.");
        } else {
            System.out.println("⚠️ No orders found for this receipt.");
        }

        // Fetch and delete Delivery records
        System.out.println("🔍 Step 3: Checking delivery records...");
        List<Long> deliveryResult = entityManager
                .createNativeQuery("SELECT deliid FROM delivery_tbl WHERE receiptid = ?")
                .setParameter(1, receiptID)
                .getResultList();

        if (!deliveryResult.isEmpty()) {
            Long deliveryID = deliveryResult.get(0);
            System.out.println("✅ Found delivery ID: " + deliveryID);

            entityManager.createNativeQuery("DELETE FROM deli_track_tbl WHERE deliid = ?")
                    .setParameter(1, deliveryID)
                    .executeUpdate();
            entityManager.flush();

            entityManager.createNativeQuery("DELETE FROM delivery_tbl WHERE receiptid = ?")
                    .setParameter(1, receiptID)
                    .executeUpdate();
            entityManager.flush();
            System.out.println("✅ Delivery records deleted.");
        } else {
            System.out.println("⚠️ No delivery records found for this receipt.");
        }

        // Delete Payment records
        System.out.println("🔍 Step 4: Deleting payment records...");
        entityManager.createNativeQuery("DELETE FROM payment_tbl WHERE receiptid = ?")
                .setParameter(1, receiptID)
                .executeUpdate();
        entityManager.flush();
        System.out.println("✅ Payment records deleted.");

        // Delete Notifications
        System.out.println("🔍 Step 5: Deleting notification records...");
        entityManager.createNativeQuery("DELETE FROM notification_tbl WHERE receiptid = ?")
                .setParameter(1, receiptID)
                .executeUpdate();
        entityManager.flush();
        System.out.println("✅ Notifications deleted.");

        // Delete the Receipt
        System.out.println("🔍 Step 6: Deleting receipt record...");
        entityManager.createNativeQuery("DELETE FROM receipt_tbl WHERE receiptid = ?")
                .setParameter(1, receiptID)
                .executeUpdate();
        entityManager.flush();
        System.out.println("✅ Receipt deleted.");

        return true;
    }
}
