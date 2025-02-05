package com.example.demo.entity.Auction;

import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalDateTime;

import com.example.demo.entity.Item;

@Entity
@Table(name = "auction_tbl")
public class Auction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long auctionID;

	private Double incrementAmount;

	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime createdAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AuctionStatus stat;

	@ManyToOne
	@JoinColumn(name = "itemID", nullable = false)
	private Item item;

	private Double startPrice;

	private LocalDateTime endTime;

	// Enum for auction status
	public enum AuctionStatus {
		ACTIVE, COMPLETED, CANCELED
	}

	// Getters and Setters
	public Long getAuctionID() {
		return auctionID;
	}

	public void setAuctionID(Long auctionID) {
		this.auctionID = auctionID;
	}

	public Double getIncrementAmount() {
		return incrementAmount;
	}

	public void setIncrementAmount(Double incrementAmount) {
		this.incrementAmount = incrementAmount;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public AuctionStatus getStat() {
		return stat;
	}

	public void setStat(AuctionStatus stat) {
		this.stat = stat;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public Double getStartPrice() {
		return startPrice;
	}

	public void setStartPrice(Double startPrice) {
		this.startPrice = startPrice;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public String getRemainingTime() {
		if (endTime == null)
			return "N/A";

		LocalDateTime now = LocalDateTime.now();
		Duration duration = Duration.between(now, endTime);

		long seconds = duration.getSeconds();
		if (seconds <= 0)
			return "Expired";

		long days = seconds / (24 * 3600);
		long hours = (seconds % (24 * 3600)) / 3600;
		long minutes = (seconds % 3600) / 60;
		long remainingSeconds = seconds % 60;

		return String.format("%d days %02d:%02d:%02d", days, hours, minutes, remainingSeconds);
	}

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

}
