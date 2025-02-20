package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_tbl")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long orderID;

	@ManyToOne
	@JoinColumn(name = "receiptID", nullable = false) // ✅ Link to Receipt
	private Receipt receipt;

	@ManyToOne
	@JoinColumn(name = "userID_buyer", nullable = false) // ✅ Buyer
	private User buyer;

	@ManyToOne
	@JoinColumn(name = "userID_seller", nullable = false) // ✅ Seller
	private User seller;

	@ManyToOne
	@JoinColumn(name = "itemID", nullable = false) // ✅ Item Purchased
	private Item item;

	@Column(nullable = false)
	private int quantity;

	@Column(nullable = false)
	private double price; // ✅ Store price at time of purchase

	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime createdAt;

	public Order() {
		this.createdAt = LocalDateTime.now();
	}

	// ✅ Getters and Setters
	public Long getOrderID() {
		return orderID;
	}

	public void setOrderID(Long orderID) {
		this.orderID = orderID;
	}

	public Receipt getReceipt() {
		return receipt;
	}

	public void setReceipt(Receipt receipt) {
		this.receipt = receipt;
	}

	public User getBuyer() {
		return buyer;
	}

	public void setBuyer(User buyer) {
		this.buyer = buyer;
	}

	public User getSeller() {
		return seller;
	}

	public void setSeller(User seller) {
		this.seller = seller;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
