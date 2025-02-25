package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment_tbl")
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long paymentID;

	@ManyToOne
	@JoinColumn(name = "receiptID", nullable = false)
	private Receipt receipt; // ✅ Links to receipt_tbl

	@ManyToOne
	@JoinColumn(name = "userID", nullable = false)
	private User user; // ✅ Buyer who made the payment

	@ManyToOne
	@JoinColumn(name = "cardID", nullable = true) // ✅ Nullable for cash payments
	private CreditCard creditCard;

	public CreditCard getCreditCard() {
		return creditCard;
	}

	public void setCreditCard(CreditCard creditCard) {
		this.creditCard = creditCard;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal amount; // ✅ Total amount paid

	@Column(nullable = false, length = 20)
	private String paymentStatus = "PENDING"; // ✅ Default status

	@Column(nullable = false, length = 50)
	private String paymentMethod; // ✅ Example: CASH_ON_DELIVERY, CARD

	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime createdAt; // ✅ Timestamp when payment was made

	@Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private LocalDateTime updatedAt; // ✅ Timestamp when payment status was updated

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = this.createdAt;
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	// ✅ Constructors
	public Payment() {
	}

	public Payment(Receipt receipt, User user, BigDecimal amount, String paymentMethod) {
		this.receipt = receipt;
		this.user = user;
		this.amount = amount;
		this.paymentMethod = paymentMethod;
		this.paymentStatus = "PENDING";
	}

	// ✅ Getters & Setters
	public Long getPaymentID() {
		return paymentID;
	}

	public void setPaymentID(Long paymentID) {
		this.paymentID = paymentID;
	}

	public Receipt getReceipt() {
		return receipt;
	}

	public void setReceipt(Receipt receipt) {
		this.receipt = receipt;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
