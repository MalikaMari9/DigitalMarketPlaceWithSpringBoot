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
@Table(name = "seller_tbl")
public class Seller {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long sellerID;

	@Column(nullable = false, unique = true, length = 50)
	private String businessName;

	@Column(nullable = true, length = 10)
	private String businessType; // Can be 'B2C' or 'C2C'

	@Column(nullable = false, columnDefinition = "ENUM('pending','accepted','rejected') DEFAULT 'pending'")
	private String approval = "pending"; // Default to "pending"

	@Column(nullable = true)
	private LocalDateTime approvalDate;

	@OneToOne
	@JoinColumn(name = "userID", nullable = false)
	private User user; // Foreign Key reference to User entity

	@Column(length = 255)
	private String name;

	@Column(length = 45)
	private String nrcNo;

	@Column(length = 255)
	private String nrcBack;

	@Column(length = 255)
	private String nrcFront;

	// Constructors
	public Seller() {
	}

	public Seller(User user, String businessType, String name, String nrcNo, String nrcBack, String nrcFront,
			String businessName) {
		this.user = user;
		this.businessName = businessName;
		this.businessType = businessType;
		this.name = name;
		this.nrcNo = nrcNo;
		this.nrcBack = nrcBack;
		this.nrcFront = nrcFront;
		this.approval = "pending"; // Default approval status
	}

	// Getters and Setters
	public Long getSellerID() {
		return sellerID;
	}

	public void setSellerID(Long sellerID) {
		this.sellerID = sellerID;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public String getApproval() {
		return approval;
	}

	public void setApproval(String approval) {
		this.approval = approval;
	}

	public LocalDateTime getApprovalDate() {
		return approvalDate;
	}

	public void setApprovalDate(LocalDateTime approvalDate) {
		this.approvalDate = approvalDate;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNrcNo() {
		return nrcNo;
	}

	public void setNrcNo(String nrcNo) {
		this.nrcNo = nrcNo;
	}

	public String getNrcBack() {
		return nrcBack;
	}

	public void setNrcBack(String nrcBack) {
		this.nrcBack = nrcBack;
	}

	public String getNrcFront() {
		return nrcFront;
	}

	public void setNrcFront(String nrcFront) {
		this.nrcFront = nrcFront;
	}

	// Conversion
	public boolean canRevertToBuyer() {
		return "C2C".equalsIgnoreCase(this.businessType);
	}
}
