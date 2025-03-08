package com.example.demo.entity;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.entity.tag.ItemTag;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "item_tbl")
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long itemID;

	@Column(length = 100)
	private String itemName;

	@Column(columnDefinition = "TEXT")
	private String descrip;

	private double price;

	private int quality;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SellType sellType;

	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime createdAt;

	@Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private LocalDateTime updatedAt;

	@Enumerated(EnumType.STRING)
	private Status stat;

	@Enumerated(EnumType.STRING)
	private Condition cond;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ApprovalStatus approve = ApprovalStatus.PENDING; // Default to PENDING

	@ManyToOne
	@JoinColumn(name = "seller_userID", nullable = false)
	private User seller;

	@ManyToOne
	@JoinColumn(name = "catID", nullable = false)
	private Category category;

	// Link to ItemImage (One-to-Many)
	@OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ItemImage> images;

	@OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ItemTag> itemTags; // ✅ Relation with `itemtag_tbl`

	@OneToOne(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
	private ItemApproval itemApproval;

	@OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Wishlist> wishlistedByUsers;

	public List<ItemImage> getImages() {
		return images;
	}

	public ItemApproval getItemApproval() {
		return itemApproval;
	}

	public void setItemApproval(ItemApproval itemApproval) {
		this.itemApproval = itemApproval;
	}

	public void setImages(List<ItemImage> images) {
		this.images = images;
	}

	// Enum Definitions
	public enum SellType {
		AUCTION, NORMAL
	}

	public enum Status {
		AVAILABLE, SOLD
	}

	public enum Condition {
		NEW, OLD
	}

	public enum ApprovalStatus {
		PENDING, APPROVED, REJECTED
	}

	// Getters and Setters
	public Long getItemID() {
		return itemID;
	}

	public void setItemID(Long itemID) {
		this.itemID = itemID;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getDescrip() {
		return descrip;
	}

	public void setDescrip(String descrip) {
		this.descrip = descrip;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public SellType getSellType() {
		return sellType;
	}

	public void setSellType(SellType sellType) {
		this.sellType = sellType;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Status getStat() {
		return stat;
	}

	public void setStat(Status stat) {
		this.stat = stat;
	}

	public Condition getCond() {
		return cond;
	}

	public void setCond(Condition cond) {
		this.cond = cond;
	}

	public User getSeller() {
		return seller;
	}

	public void setSeller(User seller) {
		this.seller = seller;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public ApprovalStatus getApprove() {
		return approve;
	}

	public void setApprove(ApprovalStatus approve) {
		this.approve = approve;
	}

	public List<ItemTag> getItemTags() {
		return itemTags;
	}

	public void setItemTags(List<ItemTag> itemTags) {
		this.itemTags = itemTags;
	}

	public List<Wishlist> getWishlistedByUsers() {
		return wishlistedByUsers;
	}

	public void setWishlistedByUsers(List<Wishlist> wishlistedByUsers) {
		this.wishlistedByUsers = wishlistedByUsers;
	}

	public String getThumbnail() {
		String basePath = "src/main/resources/static/Image/Item/" + this.itemID;
		File folder = new File(basePath);

		// Check if the directory exists and contains images
		if (folder.exists() && folder.isDirectory()) {
			File[] files = folder.listFiles((dir, name) -> name.matches(".*\\.(png|jpg|jpeg|gif)$"));
			if (files != null && files.length > 0) {
				return "/Image/Item/" + this.itemID + "/" + files[0].getName(); // Return first image found
			}
		}

		return "/Image/default_thumbnail.jpg"; // Fallback if no images exist
	}

	public String getTimeAgo() {
		if (updatedAt == null)
			return "Unknown";

		LocalDateTime now = LocalDateTime.now();
		Duration duration = Duration.between(updatedAt, now);

		long seconds = duration.getSeconds();
		if (seconds < 60)
			return seconds + " seconds ago";
		long minutes = seconds / 60;
		if (minutes < 60)
			return minutes + " minutes ago";
		long hours = minutes / 60;
		if (hours < 24)
			return hours + " hours ago";
		long days = hours / 24;
		if (days < 30)
			return days + " days ago";
		long months = days / 30;
		if (months < 12)
			return months + " months ago";
		long years = months / 12;
		return years + " years ago";
	}

	public boolean isSoldOut() {
		return this.quality == 0;
	}

}
