package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Order;
import com.example.demo.entity.User;

public interface OrderRepository extends JpaRepository<Order, Long> {

	// ✅ Count total orders for a seller
	Long countBySeller(User seller);

	// ✅ Sum total sales (price * quantity) for a seller
	@Query("SELECT SUM(o.price * o.quantity) FROM Order o WHERE o.seller = :seller")
	Double sumTotalSalesBySeller(@Param("seller") User seller);

	@Query("SELECT SUM(o.price * o.quantity) FROM Order o WHERE o.seller = :seller AND MONTH(o.createdAt) = :month")
	Double getMonthlySales(@Param("seller") User seller, @Param("month") int month);

	// Fetch top-selling products for B2C
	@Query("""
			    SELECT o.item, COUNT(o.orderID) AS totalSales
			    FROM Order o
			    WHERE o.seller = :seller
			    GROUP BY o.item
			    ORDER BY totalSales DESC
			    LIMIT 5
			""")
	List<Object[]> findTopSellingProducts(@Param("seller") User seller);

	// Fetch recently sold products for C2C
	@Query("""
			    SELECT o.item, o.createdAt, o.quantity
			    FROM Order o
			    WHERE o.seller = :seller
			    ORDER BY o.createdAt DESC
			    LIMIT 5
			""")
	List<Object[]> findRecentlySoldProducts(@Param("seller") User seller);

	@Query("SELECT COUNT(o) FROM Order o")
	Long countTotalOrders();

	// ✅ Calculate total sales (sum of all price * quantity)
	@Query("SELECT SUM(o.price * o.quantity) FROM Order o")
	Double sumTotalSales();

	@Query("SELECT SUM(o.price * o.quantity) FROM Order o WHERE MONTH(o.createdAt) = :month")
	Double getMonthlySales(int month);

	@Query("SELECT u.userID, u.username, COALESCE(SUM(o.price * o.quantity), 0) AS totalSales " + "FROM Order o "
			+ "JOIN User u ON o.seller.userID = u.userID " + "WHERE MONTH(o.createdAt) = MONTH(CURRENT_DATE) "
			+ "AND YEAR(o.createdAt) = YEAR(CURRENT_DATE) " + "GROUP BY u.userID, u.username "
			+ "ORDER BY totalSales DESC LIMIT 1")
	List<Object[]> findBestSellerOfMonth();

	@Query("SELECT SUM(o.price * o.quantity) FROM Order o WHERE MONTH(o.createdAt) = MONTH(CURRENT_DATE) AND YEAR(o.createdAt) = YEAR(CURRENT_DATE)")
	Double getTotalSalesThisMonth();

	@Query("SELECT SUM(o.quantity) FROM Order o WHERE MONTH(o.createdAt) = MONTH(CURRENT_DATE) AND YEAR(o.createdAt) = YEAR(CURRENT_DATE)")
	Integer getTotalProductsSoldThisMonth();

	@Query("SELECT SUM(o.price * o.quantity) FROM Order o WHERE MONTH(o.createdAt) = MONTH(CURRENT_DATE) AND YEAR(o.createdAt) = YEAR(CURRENT_DATE)")
	Double getTotalRevenueThisMonth();

	@Query("SELECT COUNT(o) FROM Order o WHERE o.seller.userID = :sellerID")
	int countOrdersBySeller(@Param("sellerID") Long sellerID);

	// ✅ Total Sales (Revenue) for Seller
	@Query("SELECT SUM(o.price * o.quantity) FROM Order o WHERE o.seller.userID = :sellerID")
	Double sumSalesBySeller(@Param("sellerID") Long sellerID);

	// ✅ Monthly Order Count for Seller
	@Query("SELECT MONTH(o.createdAt), COUNT(o) FROM Order o WHERE o.seller.userID = :sellerID "
			+ "GROUP BY MONTH(o.createdAt) ORDER BY MONTH(o.createdAt)")
	List<Object[]> findMonthlyOrdersBySeller(@Param("sellerID") Long sellerID);

	// ✅ Monthly Sales Revenue for Seller
	@Query("SELECT MONTH(o.createdAt), SUM(o.price * o.quantity) FROM Order o WHERE o.seller.userID = :sellerID "
			+ "GROUP BY MONTH(o.createdAt) ORDER BY MONTH(o.createdAt)")
	List<Object[]> findMonthlySalesBySeller(@Param("sellerID") Long sellerID);

	@Query("SELECT COUNT(DISTINCT o.orderID) FROM Order o WHERE o.buyer.userID = :buyerID")
	int countDistinctOrdersByBuyer(@Param("buyerID") Long buyerID);

	// ✅ Sum total amount spent BY the buyer
	@Query("SELECT COALESCE(SUM(o.price * o.quantity), 0) FROM Order o WHERE o.buyer.userID = :buyerID")
	double sumTotalSpentByBuyer(@Param("buyerID") Long buyerID);

	// ✅ Sum total sales made by the seller
	@Query("SELECT COALESCE(SUM(o.price * o.quantity), 0) FROM Order o WHERE o.seller.userID = :sellerID")
	double sumTotalSalesBySeller(@Param("sellerID") Long sellerID);

}
