package com.example.demo.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "view_tbl")
public class View {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long viewID;

	@ManyToOne
	@JoinColumn(name = "userID", nullable = true) // Nullable for guests
	private User user;

	@ManyToOne
	@JoinColumn(name = "itemID", nullable = false)
	private Item item;

	@Column(name = "viewed_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private Timestamp viewedAt;

	// Constructors
	public View() {
	}

	public View(User user, Item item) {
		this.user = user;
		this.item = item;
		this.viewedAt = new Timestamp(System.currentTimeMillis());
	}

	// Getters & Setters
	public Long getViewID() {
		return viewID;
	}

	public void setViewID(Long viewID) {
		this.viewID = viewID;
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

	public Timestamp getViewedAt() {
		return viewedAt;
	}

	public void setViewedAt(Timestamp viewedAt) {
		this.viewedAt = viewedAt;
	}
}
