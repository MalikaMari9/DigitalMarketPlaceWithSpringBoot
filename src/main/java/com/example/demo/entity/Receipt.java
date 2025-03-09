package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "receipt_tbl")
public class Receipt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long receiptID;

	@ManyToOne
	@JoinColumn(name = "userID_buyer", nullable = false) // ✅ Buyer (Customer)
	private User buyer;

	@ManyToOne
	@JoinColumn(name = "userID_seller", nullable = false) // ✅ Seller (Shop Owner)
	private User seller;

	@Column(nullable = false)
	private double totalPrice; // ✅ Total price of all items in this receipt

	@Column(nullable = false)
	private double deliFee; // ✅ Delivery Fee

	@Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Order> orders;

	@OneToOne(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
	private Delivery delivery;

	@OneToOne(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
	private Payment payment; // ✅ Directly link to Payment

	public Payment getPayment() {
		return payment;
	}

	public void setPayment(Payment payment) {
		this.payment = payment;
	}

	// ✅ Helper Method to Fetch Payment Status
	public String getPaymentStatus() {
		return payment != null ? payment.getPaymentStatus() : "PENDING";
	}

	public Receipt() {
		this.createdAt = LocalDateTime.now();
	}

	// ✅ Getters and Setters
	public Long getReceiptID() {
		return receiptID;
	}

	public void setReceiptID(Long receiptID) {
		this.receiptID = receiptID;
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

	public double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public double getDeliFee() {
		return deliFee;
	}

	public void setDeliFee(double deliFee) {
		this.deliFee = deliFee;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public List<Order> getOrders() {
		return orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

	public Delivery getDelivery() {
		return delivery;
	}

	public void setDelivery(Delivery delivery) {
		this.delivery = delivery;
	}
}
