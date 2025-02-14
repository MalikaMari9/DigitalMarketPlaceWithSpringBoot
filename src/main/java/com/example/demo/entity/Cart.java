package com.example.demo.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cart_tbl")
public class Cart {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long cartID;

	private Integer quantity;

	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime createdAt;

	@ManyToOne
	@JoinColumn(name = "itemID", nullable = false)
	private Item item;

	@ManyToOne
	@JoinColumn(name = "userID", nullable = false)
	private User user;

	// Getters and Setters
	public Long getCartID() {
		return cartID;
	}

	public void setCartID(Long cartID) {
		this.cartID = cartID;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getTimeAgo() {
		if (createdAt == null)
			return "Unknown";

		LocalDateTime now = LocalDateTime.now();
		Duration duration = Duration.between(createdAt, now);

		long seconds = duration.getSeconds();
		if (seconds < 60)
			return seconds + " seconds ago";
		long minutes = seconds / 60;
		if (minutes < 60)
			return minutes + " minutes ago";
		long hours = minutes / 60;
		if (hours < 24)
			return hours + " hours ago";
		long days = hours / 24;
		if (days == 1)
			return "Yesterday";
		if (days < 7)
			return days + " days ago";
		long weeks = days / 7;
		if (weeks < 4)
			return weeks + " weeks ago";
		long months = days / 30;
		if (months < 12)
			return months + " months ago";
		long years = months / 12;
		return years + " years ago";
	}

	// ✅ Option to format the exact date-time if needed
	public String getFormattedCreatedAt() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
		return createdAt != null ? createdAt.format(formatter) : "Unknown date";
	}
}
