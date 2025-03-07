package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Review;
import com.example.demo.entity.User;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

	// 🔹 Find all reviews written by a specific user (reviewer)
	List<Review> findByReviewer(User reviewer);

	// 🔹 Find all reviews received by a specific user (reviewed)
	List<Review> findByReviewed(User reviewed);

	// 🔹 Check if a user has already reviewed another user
	Optional<Review> findByReviewerAndReviewed(User reviewer, User reviewed);

	// 🔹 Get the average rating of a user (rounded to 2 decimal places)
	@Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.reviewed = :user")
	double getAverageRating(User user);

	List<Review> findByReviewed_UserID(Long sellerID);

	// ✅ Calculate the average rating for a seller
	@Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewed.userID = :sellerID")
	Double getAverageRating(@Param("sellerID") Long sellerID);

	@Query("SELECT r.reviewed.userID, AVG(r.rating), COUNT(r) FROM Review r GROUP BY r.reviewed.userID")
	List<Object[]> findAllAverageRatings();

	@Query("SELECT r FROM Review r WHERE r.reviewed.userID = :sellerID")
	List<Review> findByReviewedUser(@Param("sellerID") Long sellerID);

}
