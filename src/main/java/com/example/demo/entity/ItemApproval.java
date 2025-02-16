package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "item_approval_tbl")
public class ItemApproval {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long approvalID; // ✅ Primary key

	@OneToOne
	@JoinColumn(name = "itemID", nullable = false, unique = true)
	private Item item; // ✅ Foreign key to Item

	@Column(columnDefinition = "TEXT")
	private String rejectionReason; // ✅ Optional rejection reason

	@Column(nullable = true)
	private LocalDateTime approvalDate; // ✅ Date of approval/rejection

	// ✅ Getters and Setters
	public Long getApprovalID() {
		return approvalID;
	}

	public void setApprovalID(Long approvalID) {
		this.approvalID = approvalID;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}

	public LocalDateTime getApprovalDate() {
		return approvalDate;
	}

	public void setApprovalDate(LocalDateTime approvalDate) {
		this.approvalDate = approvalDate;
	}
}
