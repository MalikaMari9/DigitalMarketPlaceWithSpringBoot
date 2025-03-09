package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Delivery;
import com.example.demo.entity.Receipt;
import com.example.demo.entity.User;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

	List<Receipt> findByBuyer(User user);

	List<Receipt> findBySeller(User seller);

	@Query("SELECT r FROM Receipt r " + "WHERE r.seller = :seller "
			+ "AND (LOWER(r.buyer.username) LIKE LOWER(CONCAT('%', :search, '%')) "
			+ "OR EXISTS (SELECT o FROM Order o WHERE o.receipt = r AND LOWER(o.item.itemName) LIKE LOWER(CONCAT('%', :search, '%'))))")
	Page<Receipt> findBySellerAndSearch(@Param("seller") User seller, @Param("search") String search,
			Pageable pageable);

	Page<Receipt> findByBuyerUsernameContainingIgnoreCaseOrSellerUsernameContainingIgnoreCase(String buyer,
			String seller, Pageable pageable);

	Page<Receipt> findBySeller(User seller, Pageable pageable);

	Page<Receipt> findByBuyer(User buyer, Pageable pageable);

	@Query("SELECT r FROM Receipt r WHERE r.seller = :seller AND r.delivery.status = :status")
	Page<Receipt> findBySellerAndDeliveryStatus(@Param("seller") User seller,
			@Param("status") Delivery.DeliveryStatus status, Pageable pageable);

}
