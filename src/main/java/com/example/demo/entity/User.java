package com.example.demo.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_tbl")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userID;

	@Column(nullable = false, unique = true, length = 100)
	private String username;

	@Column(nullable = false, length = 256)
	private String password;

	@Column(length = 256)
	private String email;

	@Column(length = 20)
	private String phone;

	@Column(length = 255)
	private String profilePath = "default.png";

	private LocalDate dob;

	@Column(length = 10)
	private String gender;

	@Column(columnDefinition = "TEXT")
	private String bio;

	@Column(nullable = false, length = 20)
	private String role = "BUYER";

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public Seller getSeller() {
		return seller;
	}

	public void setSeller(Seller seller) {
		this.seller = seller;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Seller seller;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Wishlist> wishlistItems;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	// Getters and Setters
	public Long getUserID() {
		return userID;
	}

	public void setUserID(Long userID) {
		this.userID = userID;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getProfilePath() {
		return profilePath;
	}

	public void setProfilePath(String profilePath) {
		this.profilePath = profilePath;
	}

	public LocalDate getDob() {
		return dob;
	}

	public void setDob(LocalDate dob) {
		this.dob = dob;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public List<Wishlist> getWishlistItems() {
		return wishlistItems;
	}

	public void setWishlistItems(List<Wishlist> wishlistItems) {
		this.wishlistItems = wishlistItems;
	}

	// Seller Buyer Conversion
	public boolean canSwitchToBuyer() {
		return this.seller != null && "C2C".equalsIgnoreCase(this.seller.getBusinessType());
	}

	// ✅ Convert Buyer to C2C Seller
	public void convertToSeller(Seller sellerData) {
		this.seller = sellerData;
		this.role = "SELLER";
	}

	// ✅ Convert C2C Seller back to Buyer
	public void convertToBuyer() {
		if (canSwitchToBuyer()) {
			this.seller = null; // Remove seller profile
			this.role = "BUYER";
		}
	}
}
