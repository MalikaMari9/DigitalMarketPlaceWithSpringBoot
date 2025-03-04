package com.example.demo.entity;

import java.time.LocalDateTime;

import com.example.demo.entity.Delivery.DeliveryStatus;

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
@Table(name = "deli_track_tbl")
public class DeliTrack {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long delitrackID;

	@ManyToOne
	@JoinColumn(name = "deliID", nullable = false) // Links to Delivery
	private Delivery delivery;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DeliveryStatus status; // ✅ Enum matches `Delivery` status

	@Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private LocalDateTime updatedAt;

	@Column(length = 255)
	private String note; // ✅ Optional remarks (e.g., "Out for delivery")

	public DeliTrack() {
		this.updatedAt = LocalDateTime.now();
	}

	public DeliTrack(Delivery delivery, DeliveryStatus status, String note) {
		this.delivery = delivery;
		this.status = status;
		this.note = note;
		this.updatedAt = LocalDateTime.now();
	}

	// ✅ Automatically update `updatedAt` on status change
	public void setStatus(DeliveryStatus status) {
		this.status = status;
		this.updatedAt = LocalDateTime.now();
	}

	// ✅ Getters and Setters
	public Long getDelitrackID() {
		return delitrackID;
	}

	public Delivery getDelivery() {
		return delivery;
	}

	public void setDelivery(Delivery delivery) {
		this.delivery = delivery;
	}

	public DeliveryStatus getStatus() {
		return status;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}
