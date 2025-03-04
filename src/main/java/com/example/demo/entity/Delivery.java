package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "delivery_tbl")
public class Delivery {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long deliID;

	@ManyToOne
	@JoinColumn(name = "receiptID", nullable = false) // ✅ Links to Receipt
	private Receipt receipt;

	@ManyToOne
	@JoinColumn(name = "addressID", nullable = false) // ✅ Links to Address
	private Address address;

	@Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime createdAt;

	@Column(columnDefinition = "TIMESTAMP DEFAULT NULL")
	private LocalDateTime updatedAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DeliveryStatus status; // ✅ Enum for Delivery Status

	// ✅ Added "RECEIVED" status
	public enum DeliveryStatus {
		PENDING, SHIPPED, DELIVERED, RECEIVED, CANCELED
	}

	public Delivery() {
		this.createdAt = LocalDateTime.now();
		this.status = DeliveryStatus.PENDING; // Default status
	}

	// ✅ Automatically update `updatedAt` when the status changes
	public void setStatus(DeliveryStatus status) {
		this.status = status;
		this.updatedAt = LocalDateTime.now(); // ✅ Update timestamp on status change
	}

	// ✅ Getters and Setters
	public Long getDeliID() {
		return deliID;
	}

	public void setDeliID(Long deliID) {
		this.deliID = deliID;
	}

	public Receipt getReceipt() {
		return receipt;
	}

	public void setReceipt(Receipt receipt) {
		this.receipt = receipt;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public DeliveryStatus getStatus() {
		return status;
	}
}
