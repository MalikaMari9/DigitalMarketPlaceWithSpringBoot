package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "review_tbl")
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long reviewID;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewerID", nullable = false)
	private User reviewer; // User who writes the review

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewedID", nullable = false)
	private User reviewed; // User being reviewed

	@Column(nullable = false)
	private int rating; // 1-5 stars

	@Column(columnDefinition = "TEXT")
	private String comment; // Review text

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	// Getters and Setters
	public Long getReviewID() {
		return reviewID;
	}

	public void setReviewID(Long reviewID) {
		this.reviewID = reviewID;
	}

	public User getReviewer() {
		return reviewer;
	}

	public void setReviewer(User reviewer) {
		this.reviewer = reviewer;
	}

	public User getReviewed() {
		return reviewed;
	}

	public void setReviewed(User reviewed) {
		this.reviewed = reviewed;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
