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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "creditcard_tbl")
public class CreditCard {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long cardID;

	@ManyToOne
	@JoinColumn(name = "userID", nullable = false)
	private User user; // ✅ Card belongs to this user

	@Column(nullable = false, length = 4)
	private String lastFourDigits; // ✅ Stores only the last 4 digits

	@Column(nullable = false, length = 100)
	private String cardOwner; // ✅ Name on the card

	@Column(nullable = false, length = 7) // Format: MM/YYYY
	private String expirationDate;

	@Column(nullable = false, length = 10)
	private String postalCode; // ✅ Billing address postal code

	@Column(nullable = false, length = 20)
	private String cardType; // ✅ Example: VISA, MASTERCARD, AMEX

	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime createdAt; // ✅ Timestamp when card was added

	@Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private LocalDateTime updatedAt; // ✅ Timestamp when details were updated

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
	public CreditCard() {
	}

	public CreditCard(User user, String lastFourDigits, String cardOwner, String expirationDate, String postalCode,
			String cardType) {
		this.user = user;
		this.lastFourDigits = lastFourDigits;
		this.cardOwner = cardOwner;
		this.expirationDate = expirationDate;
		this.postalCode = postalCode;
		this.cardType = cardType;
	}

	// ✅ Getters & Setters
	public Long getCardID() {
		return cardID;
	}

	public void setCardID(Long cardID) {
		this.cardID = cardID;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getLastFourDigits() {
		return lastFourDigits;
	}

	public void setLastFourDigits(String lastFourDigits) {
		this.lastFourDigits = lastFourDigits;
	}

	public String getCardOwner() {
		return cardOwner;
	}

	public void setCardOwner(String cardOwner) {
		this.cardOwner = cardOwner;
	}

	public String getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(String expirationDate) {
		this.expirationDate = expirationDate;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
