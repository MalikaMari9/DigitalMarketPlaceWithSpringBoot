package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
