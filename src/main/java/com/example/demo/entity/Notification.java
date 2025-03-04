package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification_tbl")
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long notiID;

	@ManyToOne
	@JoinColumn(name = "userID", nullable = false)
	private User user; // Receiver of the notification

	@ManyToOne
	@JoinColumn(name = "senderUserID", nullable = true)
	private User sender; // Only needed for message notifications

	@ManyToOne
	@JoinColumn(name = "itemID", nullable = true)
	private Item item; // Only needed for item-related notifications

	@ManyToOne
	@JoinColumn(name = "receiptID", nullable = true) // ✅ Add this field
	private Receipt receipt;

	public Receipt getReceipt() {
		return receipt;
	}

	public void setReceipt(Receipt receipt) {
		this.receipt = receipt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	/*
	 * @ManyToOne
	 * 
	 * @JoinColumn(name = "orderID", nullable = true) private Order order; // Only
	 * needed for order-related notifications
	 */
	@Column(nullable = false, columnDefinition = "TEXT")
	private String notiText; // Message content of the notification

	@Column(nullable = false, length = 50)
	private String notiType; // Type of notification (e.g., ITEM_APPROVAL, NEW_MESSAGE)

	@Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
	private boolean seen = false; // Whether the user has seen the notification

	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime createdAt; // Timestamp when notification was created

	@Column(nullable = true)
	private LocalDateTime expiresAt; // Optional expiration date for auto-deletion

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	// ✅ Constructors
	public Notification() {
	}

	public Notification(User user, String notiText, String notiType) {
		this.user = user;
		this.notiText = notiText;
		this.notiType = notiType;
	}

	public Notification(User user, User sender, String notiText, String notiType) {
		this.user = user;
		this.sender = sender;
		this.notiText = notiText;
		this.notiType = notiType;
	}

	public Notification(User user, Item item, String notiText, String notiType) {
		this.user = user;
		this.item = item;
		this.notiText = notiText;
		this.notiType = notiType;
	}
	/*
	 * public Notification(User user, Order order, String notiText, String notiType)
	 * { this.user = user; this.order = order; this.notiText = notiText;
	 * this.notiType = notiType; }
	 * 
	 */

	// ✅ Getters & Setters
	public Long getNotiID() {
		return notiID;
	}

	public void setNotiID(Long notiID) {
		this.notiID = notiID;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	/*
	 * public Order getOrder() { return order; }
	 * 
	 * public void setOrder(Order order) { this.order = order; }
	 */
	public String getNotiText() {
		return notiText;
	}

	public void setNotiText(String notiText) {
		this.notiText = notiText;
	}

	public String getNotiType() {
		return notiType;
	}

	public void setNotiType(String notiType) {
		this.notiType = notiType;
	}

	public boolean isSeen() {
		return seen;
	}

	public void setSeen(boolean seen) {
		this.seen = seen;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}
}
