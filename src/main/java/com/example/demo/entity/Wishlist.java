package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "wishlist_tbl", uniqueConstraints = @UniqueConstraint(columnNames = { "userID", "itemID" }))
public class Wishlist {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long wishlistID;

	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime createdAt;

	@ManyToOne
	@JoinColumn(name = "userID", nullable = false, foreignKey = @ForeignKey(name = "FK_wishlist_user"))
	private User user;

	@ManyToOne
	@JoinColumn(name = "itemID", nullable = false, foreignKey = @ForeignKey(name = "FK_wishlist_item"))
	private Item item;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	public Wishlist() {
	}

	public Wishlist(User user, Item item) {
		this.user = user;
		this.item = item;
		this.createdAt = LocalDateTime.now();
	}

	// Getters and Setters
	public Long getWishlistID() {
		return wishlistID;
	}

	public void setWishlistID(Long wishlistID) {
		this.wishlistID = wishlistID;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

}
